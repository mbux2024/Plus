package com.homeflix.tv.data.remote.api

import com.homeflix.tv.data.remote.dto.tmdb.*
import retrofit2.Response
import retrofit2.http.*

/**
 * TMDB API v3 Retrofit service.
 * All endpoints use Bearer token auth via interceptor.
 */
interface TmdbApiService {

    // ─── Trending ────────────────────────────────────────────────────────

    @GET("trending/movie/{time_window}")
    suspend fun getTrendingMovies(
        @Path("time_window") timeWindow: String = "week",
        @Query("page") page: Int = 1
    ): Response<TmdbPagedResponse>

    @GET("trending/tv/{time_window}")
    suspend fun getTrendingTv(
        @Path("time_window") timeWindow: String = "week",
        @Query("page") page: Int = 1
    ): Response<TmdbPagedResponse>

    // ─── Popular ─────────────────────────────────────────────────────────

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1
    ): Response<TmdbPagedResponse>

    @GET("tv/popular")
    suspend fun getPopularTv(
        @Query("page") page: Int = 1
    ): Response<TmdbPagedResponse>

    // ─── Top Rated ───────────────────────────────────────────────────────

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1
    ): Response<TmdbPagedResponse>

    @GET("tv/top_rated")
    suspend fun getTopRatedTv(
        @Query("page") page: Int = 1
    ): Response<TmdbPagedResponse>

    // ─── Airing Today ────────────────────────────────────────────────────

    @GET("tv/airing_today")
    suspend fun getAiringTodayTv(
        @Query("page") page: Int = 1
    ): Response<TmdbPagedResponse>

    // ─── Discover (Genre + Provider filtering) ───────────────────────────

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("page") page: Int = 1,
        @Query("with_genres") genres: String? = null,
        @Query("with_watch_providers") watchProviders: String? = null,
        @Query("watch_region") watchRegion: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): Response<TmdbPagedResponse>

    @GET("discover/tv")
    suspend fun discoverTv(
        @Query("page") page: Int = 1,
        @Query("with_genres") genres: String? = null,
        @Query("with_watch_providers") watchProviders: String? = null,
        @Query("watch_region") watchRegion: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): Response<TmdbPagedResponse>

    // ─── Search ──────────────────────────────────────────────────────────

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Response<TmdbPagedResponse>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Response<TmdbPagedResponse>

    @GET("search/tv")
    suspend fun searchTv(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Response<TmdbPagedResponse>

    // ─── Movie Details ───────────────────────────────────────────────────

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "credits,videos,similar,images"
    ): Response<TmdbMovieDetailDto>

    // ─── TV Details ──────────────────────────────────────────────────────

    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") tvId: Int,
        @Query("append_to_response") appendToResponse: String = "credits,videos,similar,images"
    ): Response<TmdbTvDetailDto>

    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int
    ): Response<TmdbSeasonDetailDto>

    // ─── External IDs ────────────────────────────────────────────────────

    @GET("movie/{movie_id}/external_ids")
    suspend fun getMovieExternalIds(
        @Path("movie_id") movieId: Int
    ): Response<TmdbExternalIdsDto>

    @GET("tv/{tv_id}/external_ids")
    suspend fun getTvExternalIds(
        @Path("tv_id") tvId: Int
    ): Response<TmdbExternalIdsDto>

    // ─── Genres ──────────────────────────────────────────────────────────

    @GET("genre/movie/list")
    suspend fun getMovieGenres(): Response<TmdbGenreListDto>

    @GET("genre/tv/list")
    suspend fun getTvGenres(): Response<TmdbGenreListDto>

    // ─── Watch Providers ─────────────────────────────────────────────────

    @GET("watch/providers/movie")
    suspend fun getMovieWatchProviders(
        @Query("watch_region") region: String = "US"
    ): Response<TmdbWatchProviderListDto>

    @GET("watch/providers/tv")
    suspend fun getTvWatchProviders(
        @Query("watch_region") region: String = "US"
    ): Response<TmdbWatchProviderListDto>

    // ─── Similar / Recommendations ───────────────────────────────────────

    @GET("movie/{movie_id}/similar")
    suspend fun getSimilarMovies(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int = 1
    ): Response<TmdbPagedResponse>

    @GET("tv/{tv_id}/similar")
    suspend fun getSimilarTv(
        @Path("tv_id") tvId: Int,
        @Query("page") page: Int = 1
    ): Response<TmdbPagedResponse>

    @GET("movie/{movie_id}/recommendations")
    suspend fun getMovieRecommendations(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int = 1
    ): Response<TmdbPagedResponse>

    @GET("tv/{tv_id}/recommendations")
    suspend fun getTvRecommendations(
        @Path("tv_id") tvId: Int,
        @Query("page") page: Int = 1
    ): Response<TmdbPagedResponse>

    // ─── Images (logos for provider branding) ────────────────────────────

    @GET("movie/{movie_id}/images")
    suspend fun getMovieImages(
        @Path("movie_id") movieId: Int,
        @Query("include_image_language") language: String = "en,null"
    ): Response<TmdbImagesDto>

    @GET("tv/{tv_id}/images")
    suspend fun getTvImages(
        @Path("tv_id") tvId: Int,
        @Query("include_image_language") language: String = "en,null"
    ): Response<TmdbImagesDto>
}
