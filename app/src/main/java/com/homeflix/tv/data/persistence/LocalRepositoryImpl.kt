package com.homeflix.tv.data.persistence

import com.homeflix.tv.data.persistence.dao.AddonDao
import com.homeflix.tv.data.persistence.dao.MyListDao
import com.homeflix.tv.data.persistence.dao.WatchProgressDao
import com.homeflix.tv.data.persistence.entity.*
import com.homeflix.tv.domain.model.*
import com.homeflix.tv.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepositoryImpl @Inject constructor(
    private val myListDao: MyListDao,
    private val watchProgressDao: WatchProgressDao,
    private val addonDao: AddonDao
) : LocalRepository {

    // ─── My List ─────────────────────────────────────────────────────────

    override fun getMyList(): Flow<List<MyListItem>> =
        myListDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun isInMyList(tmdbId: Int, mediaType: TmdbMediaType): Boolean =
        myListDao.exists(tmdbId, mediaType.name)

    override suspend fun addToMyList(item: MyListItem) =
        myListDao.insert(item.toEntity())

    override suspend fun removeFromMyList(tmdbId: Int, mediaType: TmdbMediaType) =
        myListDao.delete(tmdbId, mediaType.name)

    // ─── Watch Progress ──────────────────────────────────────────────────

    override fun getContinueWatching(): Flow<List<WatchProgress>> =
        watchProgressDao.getContinueWatching().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getWatchProgress(
        tmdbId: Int,
        seasonNumber: Int?,
        episodeNumber: Int?
    ): WatchProgress? =
        watchProgressDao.getProgress(tmdbId, seasonNumber, episodeNumber)?.toDomain()

    override suspend fun saveWatchProgress(progress: WatchProgress) =
        watchProgressDao.upsert(progress.toEntity())

    override suspend fun markCompleted(tmdbId: Int, seasonNumber: Int?, episodeNumber: Int?) =
        watchProgressDao.markCompleted(tmdbId, seasonNumber, episodeNumber)

    override suspend fun removeWatchProgress(tmdbId: Int, seasonNumber: Int?, episodeNumber: Int?) =
        watchProgressDao.delete(tmdbId, seasonNumber, episodeNumber)

    // ─── Episode Watched Status ──────────────────────────────────────────

    override fun getWatchedEpisodes(tmdbId: Int, seasonNumber: Int): Flow<List<EpisodeWatchedStatus>> =
        watchProgressDao.getWatchedEpisodes(tmdbId, seasonNumber).map { entities ->
            entities.map { entity ->
                EpisodeWatchedStatus(
                    tmdbId = entity.tmdbId,
                    seasonNumber = entity.seasonNumber,
                    episodeNumber = entity.episodeNumber,
                    isWatched = entity.isWatched,
                    watchedAt = entity.watchedAt
                )
            }
        }

    override suspend fun markEpisodeWatched(tmdbId: Int, seasonNumber: Int, episodeNumber: Int) =
        watchProgressDao.upsertEpisodeWatched(
            EpisodeWatchedEntity(
                tmdbId = tmdbId,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
                isWatched = true,
                watchedAt = System.currentTimeMillis()
            )
        )

    override suspend fun markEpisodeUnwatched(tmdbId: Int, seasonNumber: Int, episodeNumber: Int) =
        watchProgressDao.deleteEpisodeWatched(tmdbId, seasonNumber, episodeNumber)

    override suspend fun isEpisodeWatched(tmdbId: Int, seasonNumber: Int, episodeNumber: Int): Boolean =
        watchProgressDao.isEpisodeWatched(tmdbId, seasonNumber, episodeNumber)

    // ─── Installed Addons ────────────────────────────────────────────────

    override fun getInstalledAddons(): Flow<List<StremioAddon>> =
        addonDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun installAddon(addon: StremioAddon) =
        addonDao.insert(addon.toEntity())

    override suspend fun removeAddon(addonId: String) =
        addonDao.delete(addonId)

    override suspend fun updateAddonPriority(addonId: String, newPriority: Int) =
        addonDao.updatePriority(addonId, newPriority)

    override suspend fun toggleAddon(addonId: String, enabled: Boolean) =
        addonDao.toggleEnabled(addonId, enabled)
}
