package com.homeflix.tv.data.remote.stremio

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.homeflix.tv.domain.model.*
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "StremioAddonClient"

/**
 * HTTP client for communicating with Stremio addon endpoints.
 * Handles manifest fetching, stream querying, and subtitle fetching.
 *
 * Stremio addon protocol:
 * - Manifest: GET {baseUrl}/manifest.json
 * - Streams:  GET {baseUrl}/stream/{type}/{id}.json
 * - Subtitles: GET {baseUrl}/subtitles/{type}/{id}.json
 *
 * For movies: id = IMDb ID (e.g., "tt1234567")
 * For series: id = IMDb ID:season:episode (e.g., "tt1234567:1:1")
 */
@Singleton
class StremioAddonClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {

    // ─── Manifest ────────────────────────────────────────────────────────

    /**
     * Fetch and parse a Stremio addon manifest.
     * @param manifestUrl Full URL to manifest.json (or base URL — will append /manifest.json)
     */
    suspend fun fetchManifest(manifestUrl: String): Result<StremioManifest> = withContext(Dispatchers.IO) {
        try {
            val url = normalizeManifestUrl(manifestUrl)
            Log.d(TAG, "Fetching manifest: $url")

            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Manifest fetch failed: HTTP ${response.code}")
                )
            }

            val body = response.body?.string()
                ?: return@withContext Result.failure(Exception("Empty manifest response"))

            val manifest = gson.fromJson(body, StremioManifest::class.java)
            Log.d(TAG, "Manifest parsed: ${manifest.name} v${manifest.version}")
            Result.success(manifest)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching manifest: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ─── Streams ─────────────────────────────────────────────────────────

    /**
     * Query an addon for streams.
     * @param baseUrl Addon base URL (without trailing slash)
     * @param type "movie" or "series"
     * @param id For movies: IMDb ID. For series: "imdbId:season:episode"
     */
    suspend fun fetchStreams(
        baseUrl: String,
        type: String,
        id: String
    ): Result<List<StremioStream>> = withContext(Dispatchers.IO) {
        try {
            val cleanBase = baseUrl.trimEnd('/')
            val url = "$cleanBase/stream/$type/$id.json"
            Log.d(TAG, "Fetching streams: $url")

            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.w(TAG, "Stream fetch failed: HTTP ${response.code} for $url")
                return@withContext Result.success(emptyList())
            }

            val body = response.body?.string()
                ?: return@withContext Result.success(emptyList())

            val streamResponse = gson.fromJson(body, StremioStreamResponse::class.java)
            Log.d(TAG, "Streams received: ${streamResponse.streams.size} from $cleanBase")
            Result.success(streamResponse.streams)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching streams from $baseUrl: ${e.message}", e)
            Result.success(emptyList()) // Don't fail — just no streams from this addon
        }
    }

    // ─── Subtitles ───────────────────────────────────────────────────────

    /**
     * Query an addon for subtitles.
     * @param baseUrl Subtitle addon base URL
     * @param type "movie" or "series"
     * @param id For movies: IMDb ID. For series: "imdbId:season:episode"
     */
    suspend fun fetchSubtitles(
        baseUrl: String,
        type: String,
        id: String
    ): Result<List<StremioSubtitle>> = withContext(Dispatchers.IO) {
        try {
            val cleanBase = baseUrl.trimEnd('/')
            val url = "$cleanBase/subtitles/$type/$id.json"
            Log.d(TAG, "Fetching subtitles: $url")

            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.w(TAG, "Subtitle fetch failed: HTTP ${response.code}")
                return@withContext Result.success(emptyList())
            }

            val body = response.body?.string()
                ?: return@withContext Result.success(emptyList())

            val subtitleResponse = gson.fromJson(body, StremioSubtitleResponse::class.java)
            Log.d(TAG, "Subtitles received: ${subtitleResponse.subtitles.size}")
            Result.success(subtitleResponse.subtitles)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching subtitles: ${e.message}", e)
            Result.success(emptyList())
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    /**
     * Normalize a manifest URL. Handles:
     * - Already ends with /manifest.json → use as-is
     * - Base URL → append /manifest.json
     * - Stremio protocol links (stremio://) → convert to https
     */
    private fun normalizeManifestUrl(url: String): String {
        var normalized = url.trim()
            .replace("stremio://", "https://")

        // Remove trailing slash
        normalized = normalized.trimEnd('/')

        // Append manifest.json if not present
        if (!normalized.endsWith("/manifest.json") && !normalized.endsWith("manifest.json")) {
            normalized = "$normalized/manifest.json"
        }

        return normalized
    }

    /**
     * Extract base URL from a manifest URL.
     */
    fun extractBaseUrl(manifestUrl: String): String {
        val normalized = normalizeManifestUrl(manifestUrl)
        return normalized.removeSuffix("/manifest.json").removeSuffix("manifest.json").trimEnd('/')
    }
}
