package com.homeflix.tv.data.resolver

import android.util.Log
import com.homeflix.tv.data.remote.debrid.DebridResolver
import com.homeflix.tv.data.remote.stremio.AddonManager
import com.homeflix.tv.data.remote.stremio.StremioAddonClient
import com.homeflix.tv.domain.model.*
import com.homeflix.tv.domain.repository.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "StreamResolver"

/**
 * Main stream resolution orchestrator.
 *
 * Full flow:
 * 1. Get external IDs (TMDB → IMDb)
 * 2. Query all active Stremio addons for streams
 * 3. Parse quality badges from release titles
 * 4. Check debrid cache status (⚡ INSTANT)
 * 5. Sort by quality score (cached first, then best quality)
 * 6. For auto-play: pick the best one and resolve to direct URL
 */
@Singleton
class StreamResolverImpl @Inject constructor(
    private val tmdbRepository: TmdbRepository,
    private val addonManager: AddonManager,
    private val addonClient: StremioAddonClient,
    private val debridResolver: DebridResolver,
    private val settingsRepository: SettingsRepository
) : StreamRepository {

    // ─── Movie Streams ───────────────────────────────────────────────────

    override suspend fun resolveMovieStreams(imdbId: String): Result<List<StreamSource>> {
        return resolveStreams(imdbId, "movie", imdbId)
    }

    // ─── Episode Streams ─────────────────────────────────────────────────

    override suspend fun resolveEpisodeStreams(
        imdbId: String,
        season: Int,
        episode: Int
    ): Result<List<StreamSource>> {
        val stremioId = "$imdbId:$season:$episode"
        return resolveStreams(imdbId, "series", stremioId)
    }

    // ─── Best Stream (Auto-Play) ─────────────────────────────────────────

    override suspend fun getBestStream(
        imdbId: String,
        mediaType: TmdbMediaType,
        season: Int?,
        episode: Int?,
        preferredQuality: PreferredQuality
    ): Result<StreamSource> {
        // Get all streams first
        val streamsResult = if (mediaType == TmdbMediaType.MOVIE) {
            resolveMovieStreams(imdbId)
        } else {
            if (season == null || episode == null) {
                return Result.failure(Exception("Season and episode required for TV"))
            }
            resolveEpisodeStreams(imdbId, season, episode)
        }

        val streams = streamsResult.getOrElse {
            return Result.failure(it)
        }

        if (streams.isEmpty()) {
            return Result.failure(Exception("No streams found"))
        }

        // Filter by preferred quality if set
        val filtered = when (preferredQuality) {
            PreferredQuality.UHD_4K -> streams.filter { it.resolution == StreamResolution.UHD_4K }
            PreferredQuality.FHD_1080P -> streams.filter { it.resolution == StreamResolution.FHD_1080P }
            PreferredQuality.HD_720P -> streams.filter { it.resolution == StreamResolution.HD_720P }
            PreferredQuality.BEST_AVAILABLE -> streams
        }

        // Pick the best (already sorted by qualityScore with cached first)
        val best = filtered.firstOrNull() ?: streams.first()

        // If this stream has an infoHash but no URL yet, resolve via debrid
        if (best.url == null && best.infoHash != null) {
            val resolvedUrl = resolveDebridStream(best.infoHash, best.fileIndex)
            return resolvedUrl.map { url ->
                best.copy(url = url)
            }
        }

        return Result.success(best)
    }

    // ─── Debrid Resolution ───────────────────────────────────────────────

    override suspend fun resolveDebridStream(
        infoHash: String,
        fileIndex: Int?
    ): Result<String> {
        val result = debridResolver.resolve(infoHash, fileIndex)
        return result.map { (url, _) -> url }
    }

    // ─── Subtitles ───────────────────────────────────────────────────────

    override suspend fun getSubtitles(
        imdbId: String,
        mediaType: TmdbMediaType,
        season: Int?,
        episode: Int?
    ): Result<List<StremioSubtitle>> {
        return try {
            val subtitleAddon = addonManager.getActiveSubtitleAddon()
                ?: return Result.success(emptyList())

            val type = mediaType.stremioType
            val id = if (mediaType == TmdbMediaType.TV && season != null && episode != null) {
                "$imdbId:$season:$episode"
            } else {
                imdbId
            }

            addonClient.fetchSubtitles(subtitleAddon.baseUrl, type, id)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching subtitles: ${e.message}", e)
            Result.success(emptyList())
        }
    }

    // ─── Core Resolution Logic ───────────────────────────────────────────

    private suspend fun resolveStreams(
        imdbId: String,
        type: String,
        stremioId: String
    ): Result<List<StreamSource>> {
        return try {
            Log.d(TAG, "Resolving streams: type=$type, id=$stremioId")

            // Step 1: Get active stream addons
            val addons = addonManager.getActiveStreamAddons()
            if (addons.isEmpty()) {
                return Result.failure(Exception("No stream addons installed. Add one in Settings → Add-ons."))
            }

            // Step 2: Query all addons in parallel
            val rawStreams = coroutineScope {
                addons.map { addon ->
                    async {
                        val streams = addonClient.fetchStreams(addon.baseUrl, type, stremioId)
                            .getOrDefault(emptyList())
                        streams.map { stream -> stream to addon }
                    }
                }.awaitAll().flatten()
            }

            Log.d(TAG, "Got ${rawStreams.size} raw streams from ${addons.size} addons")

            if (rawStreams.isEmpty()) {
                return Result.success(emptyList())
            }

            // Step 3: Parse quality and convert to StreamSource
            val parsedSources = rawStreams.mapNotNull { (stream, addon) ->
                convertToStreamSource(stream, addon)
            }

            // Step 4: Check debrid cache for sources with infoHash
            val sourcesWithCache = checkCacheStatus(parsedSources)

            // Step 5: Sort by quality score (cached first, then highest quality)
            val sorted = sourcesWithCache.sortedByDescending { it.qualityScore }

            Log.d(TAG, "Resolved ${sorted.size} streams (${sorted.count { it.isCached }} cached)")
            Result.success(sorted)
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving streams: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ─── Stream Conversion ───────────────────────────────────────────────

    private fun convertToStreamSource(
        stream: StremioStream,
        addon: StremioAddon
    ): StreamSource? {
        // Need either a URL or an infoHash
        if (stream.url == null && stream.infoHash == null) return null

        // Parse the title for quality info
        val titleToParse = stream.title ?: stream.name ?: ""
        val parsed = QualityParser.parse(titleToParse)
        val seeders = QualityParser.parseSeeders(titleToParse)

        // Extract size from behavior hints if available
        val sizeFromHints = stream.behaviorHints?.videoSize ?: 0L
        val size = if (sizeFromHints > 0) sizeFromHints else parsed.sizeBytes

        return StreamSource(
            title = titleToParse.ifBlank { stream.name ?: "Unknown" },
            url = stream.url,
            infoHash = stream.infoHash?.lowercase(),
            fileIndex = stream.fileIdx,
            addonName = addon.name,
            addonId = addon.id,
            resolution = parsed.resolution,
            videoCodec = parsed.videoCodec,
            hdrType = parsed.hdrType,
            audioCodec = parsed.audioCodec,
            audioChannels = parsed.audioChannels,
            sourceType = parsed.sourceType,
            sizeBytes = size,
            seeders = seeders,
            debridService = DebridServiceType.NONE,
            isCached = false, // Will be updated in cache check
            behaviorHints = StreamBehaviorHints(
                bingeGroup = stream.behaviorHints?.bingeGroup,
                filename = stream.behaviorHints?.filename,
                videoSize = stream.behaviorHints?.videoSize
            )
        )
    }

    // ─── Cache Check ─────────────────────────────────────────────────────

    private suspend fun checkCacheStatus(sources: List<StreamSource>): List<StreamSource> {
        // Collect all infoHashes that need checking
        val hashSources = sources.filter { it.infoHash != null }
        if (hashSources.isEmpty()) return sources

        val hashes = hashSources.mapNotNull { it.infoHash }.distinct()

        // Check both services in parallel
        val (torboxCached, rdCached) = coroutineScope {
            val tb = async { debridResolver.checkTorBoxCached(hashes) }
            val rd = async { debridResolver.checkRealDebridCached(hashes) }
            tb.await() to rd.await()
        }

        Log.d(TAG, "Cache check: ${torboxCached.size} TorBox, ${rdCached.size} RD cached")

        // Update sources with cache info
        return sources.map { source ->
            if (source.infoHash == null) return@map source

            val hash = source.infoHash.lowercase()
            val isTorBoxCached = hash in torboxCached
            val isRdCached = hash in rdCached

            when {
                isTorBoxCached -> source.copy(
                    isCached = true,
                    debridService = DebridServiceType.TORBOX
                )
                isRdCached -> source.copy(
                    isCached = true,
                    debridService = DebridServiceType.REAL_DEBRID
                )
                else -> source
            }
        }
    }
}
