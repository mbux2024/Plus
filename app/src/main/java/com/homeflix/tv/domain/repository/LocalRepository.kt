package com.homeflix.tv.domain.repository

import com.homeflix.tv.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository for local on-device persistence.
 * Handles My List, watch progress, episode watched status.
 */
interface LocalRepository {

    // ─── My List ─────────────────────────────────────────────────────────
    fun getMyList(): Flow<List<MyListItem>>
    suspend fun isInMyList(tmdbId: Int, mediaType: TmdbMediaType): Boolean
    suspend fun addToMyList(item: MyListItem)
    suspend fun removeFromMyList(tmdbId: Int, mediaType: TmdbMediaType)

    // ─── Watch Progress (Continue Watching) ──────────────────────────────
    fun getContinueWatching(): Flow<List<WatchProgress>>
    suspend fun getWatchProgress(tmdbId: Int, seasonNumber: Int? = null, episodeNumber: Int? = null): WatchProgress?
    suspend fun saveWatchProgress(progress: WatchProgress)
    suspend fun markCompleted(tmdbId: Int, seasonNumber: Int? = null, episodeNumber: Int? = null)
    suspend fun removeWatchProgress(tmdbId: Int, seasonNumber: Int? = null, episodeNumber: Int? = null)

    // ─── Episode Watched Status ──────────────────────────────────────────
    fun getWatchedEpisodes(tmdbId: Int, seasonNumber: Int): Flow<List<EpisodeWatchedStatus>>
    suspend fun markEpisodeWatched(tmdbId: Int, seasonNumber: Int, episodeNumber: Int)
    suspend fun markEpisodeUnwatched(tmdbId: Int, seasonNumber: Int, episodeNumber: Int)
    suspend fun isEpisodeWatched(tmdbId: Int, seasonNumber: Int, episodeNumber: Int): Boolean

    // ─── Installed Addons ────────────────────────────────────────────────
    fun getInstalledAddons(): Flow<List<StremioAddon>>
    suspend fun installAddon(addon: StremioAddon)
    suspend fun removeAddon(addonId: String)
    suspend fun updateAddonPriority(addonId: String, newPriority: Int)
    suspend fun toggleAddon(addonId: String, enabled: Boolean)
}
