package com.homeflix.tv.data.repository

import android.util.Log
import com.homeflix.tv.data.remote.api.TmdbApiService
import com.homeflix.tv.data.remote.dto.tmdb.*
import com.homeflix.tv.domain.model.*
import com.homeflix.tv.domain.repository.ExternalIds
import com.homeflix.tv.domain.repository.TmdbRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TmdbRepository"

@Singleton
class TmdbRepositoryImpl @Inject constructor(
    private val tmdbApi: TmdbApiService
) : TmdbRepository {

    // ─── Trending ────────────────────────────────────────────────────────

    override suspend fun getTrendingMovies(page: Int): Result<List<TmdbMedia>> =
        safeApiCall("getTrendingMovies") {
            tmdbApi.getTrendingMovies(page = page)
        }.map { it.results.mapNotNull { dto -> dto.toDomain(TmdbMediaType.MOVIE) } }

    override suspend fun getTrendingTv(page: Int): Result<List<TmdbMedia>> =
        safeApiCall("getTrendingTv") {
            tmdbApi.getTrendingTv(page = page)
        }.map { it.results.mapNotNull { dto -> dto.toDomain(TmdbMediaType.TV) } }

    // ─── Top Rated ───────────────────────────────────────────────────────

    override suspend fun getTopRatedMovies(page: Int): Result<List<TmdbMedia>> =
        safeApiCall("getTopRatedMovies") {
            tmdbApi.getTopRatedMovies(page = page)
        }.map { it.results.mapNotNull { dto -> dto.toDomain(TmdbMediaType.MOVIE) } }

    override suspend fun getTopRatedTv(page: Int): Result<List<TmdbMedia>> =
        safeApiCall("getTopRatedTv") {
            tmdbApi.getTopRatedTv(page = page)
        }.map { it.results.mapNotNull { dto -> dto.toDomain(TmdbMediaType.TV) } }

    // ─── Popular ─────────────────────────────────────────────────────────

    override suspend fun getPopularMovies(page: Int): Result<List<TmdbMedia>> =
        safeApiCall("getPopularMovies") {
            tmdbApi.getPopularMovies(page = page)
        }.map { it.results.mapNotNull { dto -> dto.toDomain(TmdbMediaType.MOVIE) } }

    override suspend fun getPopularTv(page: Int): Result<List<TmdbMedia>> =
        safeApiCall("getPopularTv") {
            tmdbApi.getPopularTv(page = page)
        }.map { it.results.mapNotNull { dto -> dto.toDomain(TmdbMediaType.TV) } }

    // ─── Airing Today ────────────────────────────────────────────────────

    override suspend fun getAiringTodayTv(page: Int): Result<List<TmdbMedia>> =
        safeApiCall("getAiringTodayTv") {
            tmdbApi.getAiringTodayTv(page = page)
        }.map { it.results.mapNotNull { dto -> dto.toDomain(TmdbMediaType.TV) } }

    // ─── Top 10 ──────────────────────────────────────────────────────────

    override suspend fun getTop10Movies(): Result<List<TmdbMedia>> =
        getTrendingMovies(1).map { it.take(10) }

    override suspend fun getTop10Tv(): Result<List<TmdbMedia>> =
        getTrendingTv(1).map { it.take(10) }

    // ─── Genre Discovery ─────────────────────────────────────────────────

    override suspend fun getMovieGenres(): Result<List<TmdbGenre>> =
        safeApiCall("getMovieGenres") {
            tmdbApi.getMovieGenres()
        }.map { it.genres.map { dto -> dto.toDomain() } }

    override suspend fun getTvGenres(): Result<List<TmdbGenre>> =
        safeApiCall("getTvGenres") {
            tmdbApi.getTvGenres()
        }.map { it.genres.map { dto -> dto.toDomain() } }

    override suspend fun discoverByGenre(
        genreId: Int,
        mediaType: TmdbMediaType,
        page: Int
    ): Result<List<TmdbMedia>> = safeApiCall("discoverByGenre") {
        when (mediaType) {
            TmdbMediaType.MOVIE -> tmdbApi.discoverMovies(page = page, genres = genreId.toString())
            TmdbMediaType.TV -> tmdbApi.discoverTv(page = page, genres = genreId.toString())
        }
    }.map { it.results.mapNotNull { dto -> dto.toDomain(mediaType) } }

    // ─── Provider/Service Discovery ──────────────────────────────────────

    override suspend fun getWatchProviders(region: String): Result<List<TmdbWatchProvider>> =
        safeApiCall("getWatchProviders") {
            tmdbApi.getMovieWatchProviders(region)
        }.map { it.results.map { dto -> dto.toDomain() } }

    override suspend fun discoverByProvider(
        providerId: Int,
        mediaType: TmdbMediaType,
        page: Int
    ): Result<List<TmdbMedia>> = safeApiCall("discoverByProvider") {
        when (mediaType) {
            TmdbMediaType.MOVIE -> tmdbApi.discoverMovies(
                page = page,
                watchProviders = providerId.toString(),
                watchRegion = "US"
            )
            TmdbMediaType.TV -> tmdbApi.discoverTv(
                page = page,
                watchProviders = providerId.toString(),
                watchRegion = "US"
            )
        }
    }.map { it.results.mapNotNull { dto -> dto.toDomain(mediaType) } }

    // ─── Search ──────────────────────────────────────────────────────────

    override suspend fun searchMulti(query: String, page: Int): Result<List<TmdbMedia>> =
        safeApiCall("searchMulti") {
            tmdbApi.searchMulti(query, page)
        }.map { it.results.mapNotNull { dto -> dto.toDomain() } }

    override suspend fun searchMovies(query: String, page: Int): Result<List<TmdbMedia>> =
        safeApiCall("searchMovies") {
            tmdbApi.searchMovies(query, page)
        }.map { it.results.mapNotNull { dto -> dto.toDomain(TmdbMediaType.MOVIE) } }

    override suspend fun searchTv(query: String, page: Int): Result<List<TmdbMedia>> =
        safeApiCall("searchTv") {
            tmdbApi.searchTv(query, page)
        }.map { it.results.mapNotNull { dto -> dto.toDomain(TmdbMediaType.TV) } }

    // ─── Details ─────────────────────────────────────────────────────────

    override suspend fun getMovieDetails(movieId: Int): Result<TmdbMediaDetail> =
        safeApiCall("getMovieDetails") {
            tmdbApi.getMovieDetails(movieId)
        }.map { it.toDomain() }

    override suspend fun getTvDetails(tvId: Int): Result<TmdbMediaDetail> =
        safeApiCall("getTvDetails") {
            tmdbApi.getTvDetails(tvId)
        }.map { it.toDomain() }

    override suspend fun getSeasonDetails(tvId: Int, seasonNumber: Int): Result<List<TmdbEpisode>> =
        safeApiCall("getSeasonDetails") {
            tmdbApi.getSeasonDetails(tvId, seasonNumber)
        }.map { it.episodes.map { dto -> dto.toDomain() } }

    // ─── External IDs ────────────────────────────────────────────────────

    override suspend fun getExternalIds(tmdbId: Int, mediaType: TmdbMediaType): Result<ExternalIds> =
        safeApiCall("getExternalIds") {
            when (mediaType) {
                TmdbMediaType.MOVIE -> tmdbApi.getMovieExternalIds(tmdbId)
                TmdbMediaType.TV -> tmdbApi.getTvExternalIds(tmdbId)
            }
        }.map { it.toDomain() }

    // ─── Similar / Recommendations ───────────────────────────────────────

    override suspend fun getSimilar(
        tmdbId: Int,
        mediaType: TmdbMediaType,
        page: Int
    ): Result<List<TmdbMedia>> = safeApiCall("getSimilar") {
        when (mediaType) {
            TmdbMediaType.MOVIE -> tmdbApi.getSimilarMovies(tmdbId, page)
            TmdbMediaType.TV -> tmdbApi.getSimilarTv(tmdbId, page)
        }
    }.map { it.results.mapNotNull { dto -> dto.toDomain(mediaType) } }

    override suspend fun getRecommendations(
        tmdbId: Int,
        mediaType: TmdbMediaType,
        page: Int
    ): Result<List<TmdbMedia>> = safeApiCall("getRecommendations") {
        when (mediaType) {
            TmdbMediaType.MOVIE -> tmdbApi.getMovieRecommendations(tmdbId, page)
            TmdbMediaType.TV -> tmdbApi.getTvRecommendations(tmdbId, page)
        }
    }.map { it.results.mapNotNull { dto -> dto.toDomain(mediaType) } }

    // ─── Helper ──────────────────────────────────────────────────────────

    private suspend fun <T> safeApiCall(
        tag: String,
        call: suspend () -> retrofit2.Response<T>
    ): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("$tag: Empty response body"))
                }
            } else {
                val errorMsg = "$tag: HTTP ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "$tag: ${e.message}", e)
            Result.failure(e)
        }
    }
}
