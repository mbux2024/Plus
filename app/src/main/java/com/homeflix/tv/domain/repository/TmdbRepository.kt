package com.homeflix.tv.domain.repository

import com.homeflix.tv.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository for TMDB catalog data.
 * Provides all movie/TV discovery, search, details, and metadata.
 */
interface TmdbRepository {

    // ─── Discovery ───────────────────────────────────────────────────────
    suspend fun getTrendingMovies(page: Int = 1): Result<List<TmdbMedia>>
    suspend fun getTrendingTv(page: Int = 1): Result<List<TmdbMedia>>
    suspend fun getTopRatedMovies(page: Int = 1): Result<List<TmdbMedia>>
    suspend fun getTopRatedTv(page: Int = 1): Result<List<TmdbMedia>>
    suspend fun getPopularMovies(page: Int = 1): Result<List<TmdbMedia>>
    suspend fun getPopularTv(page: Int = 1): Result<List<TmdbMedia>>
    suspend fun getAiringTodayTv(page: Int = 1): Result<List<TmdbMedia>>

    // ─── Top 10 ──────────────────────────────────────────────────────────
    suspend fun getTop10Movies(): Result<List<TmdbMedia>>
    suspend fun getTop10Tv(): Result<List<TmdbMedia>>

    // ─── Genre Discovery ─────────────────────────────────────────────────
    suspend fun getMovieGenres(): Result<List<TmdbGenre>>
    suspend fun getTvGenres(): Result<List<TmdbGenre>>
    suspend fun discoverByGenre(genreId: Int, mediaType: TmdbMediaType, page: Int = 1): Result<List<TmdbMedia>>

    // ─── Provider/Service Discovery ──────────────────────────────────────
    suspend fun getWatchProviders(region: String = "US"): Result<List<TmdbWatchProvider>>
    suspend fun discoverByProvider(
        providerId: Int,
        mediaType: TmdbMediaType,
        page: Int = 1
    ): Result<List<TmdbMedia>>

    // ─── Search ──────────────────────────────────────────────────────────
    suspend fun searchMulti(query: String, page: Int = 1): Result<List<TmdbMedia>>
    suspend fun searchMovies(query: String, page: Int = 1): Result<List<TmdbMedia>>
    suspend fun searchTv(query: String, page: Int = 1): Result<List<TmdbMedia>>

    // ─── Details ─────────────────────────────────────────────────────────
    suspend fun getMovieDetails(movieId: Int): Result<TmdbMediaDetail>
    suspend fun getTvDetails(tvId: Int): Result<TmdbMediaDetail>
    suspend fun getSeasonDetails(tvId: Int, seasonNumber: Int): Result<List<TmdbEpisode>>

    // ─── External IDs (for Stremio addon lookups) ────────────────────────
    suspend fun getExternalIds(tmdbId: Int, mediaType: TmdbMediaType): Result<ExternalIds>

    // ─── Similar / Recommendations ───────────────────────────────────────
    suspend fun getSimilar(tmdbId: Int, mediaType: TmdbMediaType, page: Int = 1): Result<List<TmdbMedia>>
    suspend fun getRecommendations(tmdbId: Int, mediaType: TmdbMediaType, page: Int = 1): Result<List<TmdbMedia>>
}

/**
 * External IDs from TMDB (IMDb, TVDB, etc.).
 * Critical for resolving Stremio addon stream lookups.
 */
data class ExternalIds(
    val imdbId: String? = null,
    val tvdbId: Int? = null,
    val facebookId: String? = null,
    val instagramId: String? = null,
    val twitterId: String? = null
)
