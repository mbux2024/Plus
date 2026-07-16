package com.homeflix.tv.data.repository

import com.homeflix.tv.domain.model.SubtitleTrack
import com.homeflix.tv.domain.model.AudioTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * COMPATIBILITY STUB — Old StreamingRepository.
 *
 * This exists solely to allow the old VideoPlayer.kt component to compile.
 * The new architecture resolves streams via StreamRepository + DebridResolver.
 *
 * TODO: Remove once VideoPlayer.kt is migrated to direct stream URL playback.
 */
@Singleton
class StreamingRepository @Inject constructor() {

    fun getStreamUrl(mediaId: String): Flow<Result<Any>> =
        flowOf(Result.failure(Exception("Use new StreamRepository")))

    fun getSubtitleTracks(mediaId: String): Flow<Result<List<SubtitleTrack>>> =
        flowOf(Result.success(emptyList()))

    fun getAudioTracks(mediaId: String): Flow<Result<List<AudioTrack>>> =
        flowOf(Result.success(emptyList()))

    fun getSubtitleFile(mediaId: String, trackId: String): Flow<Result<String>> =
        flowOf(Result.failure(Exception("Use Stremio subtitle addon")))

    suspend fun trackView(mediaId: String): Result<Unit> = Result.success(Unit)
}
