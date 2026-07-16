package com.homeflix.tv.data.persistence.dao

import androidx.room.*
import com.homeflix.tv.data.persistence.entity.EpisodeWatchedEntity
import com.homeflix.tv.data.persistence.entity.WatchProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchProgressDao {

    // ─── Continue Watching ────────────────────────────────────────────────

    @Query("""
        SELECT * FROM watch_progress 
        WHERE is_completed = 0 AND progress_ms > 0 
        ORDER BY last_watched_at DESC 
        LIMIT 20
    """)
    fun getContinueWatching(): Flow<List<WatchProgressEntity>>

    @Query("""
        SELECT * FROM watch_progress 
        WHERE tmdb_id = :tmdbId 
        AND (season_number IS :seasonNumber OR (:seasonNumber IS NULL AND season_number IS NULL))
        AND (episode_number IS :episodeNumber OR (:episodeNumber IS NULL AND episode_number IS NULL))
        LIMIT 1
    """)
    suspend fun getProgress(tmdbId: Int, seasonNumber: Int?, episodeNumber: Int?): WatchProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: WatchProgressEntity)

    @Query("""
        UPDATE watch_progress SET is_completed = 1 
        WHERE tmdb_id = :tmdbId 
        AND (season_number IS :seasonNumber OR (:seasonNumber IS NULL AND season_number IS NULL))
        AND (episode_number IS :episodeNumber OR (:episodeNumber IS NULL AND episode_number IS NULL))
    """)
    suspend fun markCompleted(tmdbId: Int, seasonNumber: Int?, episodeNumber: Int?)

    @Query("""
        DELETE FROM watch_progress 
        WHERE tmdb_id = :tmdbId 
        AND (season_number IS :seasonNumber OR (:seasonNumber IS NULL AND season_number IS NULL))
        AND (episode_number IS :episodeNumber OR (:episodeNumber IS NULL AND episode_number IS NULL))
    """)
    suspend fun delete(tmdbId: Int, seasonNumber: Int?, episodeNumber: Int?)

    // ─── Episode Watched Status ──────────────────────────────────────────

    @Query("""
        SELECT * FROM episode_watched 
        WHERE tmdb_id = :tmdbId AND season_number = :seasonNumber
        ORDER BY episode_number ASC
    """)
    fun getWatchedEpisodes(tmdbId: Int, seasonNumber: Int): Flow<List<EpisodeWatchedEntity>>

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM episode_watched 
            WHERE tmdb_id = :tmdbId AND season_number = :seasonNumber AND episode_number = :episodeNumber AND is_watched = 1
        )
    """)
    suspend fun isEpisodeWatched(tmdbId: Int, seasonNumber: Int, episodeNumber: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEpisodeWatched(entity: EpisodeWatchedEntity)

    @Query("""
        DELETE FROM episode_watched 
        WHERE tmdb_id = :tmdbId AND season_number = :seasonNumber AND episode_number = :episodeNumber
    """)
    suspend fun deleteEpisodeWatched(tmdbId: Int, seasonNumber: Int, episodeNumber: Int)
}
