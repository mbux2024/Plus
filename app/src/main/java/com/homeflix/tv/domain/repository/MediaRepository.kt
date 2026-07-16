package com.homeflix.tv.domain.repository

import com.homeflix.tv.domain.model.TmdbMedia
import com.homeflix.tv.domain.model.TmdbGenre
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * COMPATIBILITY STUB — Old MediaRepository interface.
 *
 * This exists solely to allow the old VideoPlayer.kt component and any
 * other legacy code to compile. Methods return empty/no-op results.
 * The new architecture uses TmdbRepository, StreamRepository, and LocalRepository.
 *
 * TODO: Remove this once VideoPlayer.kt is fully migrated to new stream URL approach.
 */
interface MediaRepository {

    fun getAllMedia(
        limit: Int = 100,
        offset: Int = 0,
        genre: String? = null,
        type: String? = null
    ): Flow<Result<List<TmdbMedia>>> = flowOf(Result.success(emptyList()))

    fun getMovies(limit: Int = 100, offset: Int = 0): Flow<Result<List<TmdbMedia>>> =
        flowOf(Result.success(emptyList()))

    fun getTVShows(limit: Int = 100, offset: Int = 0): Flow<Result<List<TmdbMedia>>> =
        flowOf(Result.success(emptyList()))

    fun getMediaById(id: String): Flow<Result<TmdbMedia>> =
        flowOf(Result.failure(Exception("Not implemented - use TmdbRepository")))

    fun getMediaByGenre(genre: String, limit: Int = 100, offset: Int = 0): Flow<Result<List<TmdbMedia>>> =
        flowOf(Result.success(emptyList()))

    fun searchMedia(query: String, limit: Int = 50, offset: Int = 0): Flow<Result<List<TmdbMedia>>> =
        flowOf(Result.success(emptyList()))

    fun getAllGenres(): Flow<Result<List<TmdbGenre>>> =
        flowOf(Result.success(emptyList()))

    suspend fun updatePlaybackProgress(
        mediaId: Int,
        position: Long,
        duration: Long,
        userId: String = "1"
    ): Result<Unit> = Result.success(Unit)

    suspend fun getPlaybackProgress(mediaId: String): Result<Nothing?> = Result.success(null)
}
