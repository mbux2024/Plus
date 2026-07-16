package com.homeflix.tv.domain.repository

import com.homeflix.tv.domain.model.*

/**
 * Repository for stream resolution.
 * Orchestrates: addon query → quality parse → debrid resolve → direct URL.
 */
interface StreamRepository {

    /**
     * Resolve streams for a movie.
     * @param imdbId IMDb ID (e.g., "tt1234567")
     * @return List of stream sources sorted by quality score, with cached sources first.
     */
    suspend fun resolveMovieStreams(imdbId: String): Result<List<StreamSource>>

    /**
     * Resolve streams for a TV episode.
     * @param imdbId IMDb ID of the series
     * @param season Season number
     * @param episode Episode number
     * @return List of stream sources sorted by quality score, with cached sources first.
     */
    suspend fun resolveEpisodeStreams(
        imdbId: String,
        season: Int,
        episode: Int
    ): Result<List<StreamSource>>

    /**
     * Get the best available stream (auto-play).
     * Prefers: cached > highest quality > not CAM/TS.
     */
    suspend fun getBestStream(
        imdbId: String,
        mediaType: TmdbMediaType,
        season: Int? = null,
        episode: Int? = null,
        preferredQuality: PreferredQuality = PreferredQuality.BEST_AVAILABLE
    ): Result<StreamSource>

    /**
     * Resolve a raw infoHash to a direct stream URL via debrid services.
     * Tries TorBox first, then Real-Debrid.
     */
    suspend fun resolveDebridStream(
        infoHash: String,
        fileIndex: Int? = null
    ): Result<String> // Returns direct stream URL

    /**
     * Get subtitles from configured subtitle addon.
     */
    suspend fun getSubtitles(
        imdbId: String,
        mediaType: TmdbMediaType,
        season: Int? = null,
        episode: Int? = null
    ): Result<List<StremioSubtitle>>
}
