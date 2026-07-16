package com.homeflix.tv.data.persistence.entity

import androidx.room.*
import com.homeflix.tv.domain.model.*

/**
 * Room entities for local persistence.
 */

// ─── My List ─────────────────────────────────────────────────────────────────

@Entity(
    tableName = "my_list",
    indices = [Index(value = ["tmdb_id", "media_type"], unique = true)]
)
data class MyListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "tmdb_id") val tmdbId: Int,
    @ColumnInfo(name = "media_type") val mediaType: String, // "MOVIE" or "TV"
    val title: String,
    @ColumnInfo(name = "poster_path") val posterPath: String? = null,
    @ColumnInfo(name = "backdrop_path") val backdropPath: String? = null,
    @ColumnInfo(name = "vote_average") val voteAverage: Double = 0.0,
    val overview: String? = null,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis()
)

fun MyListEntity.toDomain(): MyListItem = MyListItem(
    tmdbId = tmdbId,
    mediaType = TmdbMediaType.valueOf(mediaType),
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    voteAverage = voteAverage,
    overview = overview,
    addedAt = addedAt
)

fun MyListItem.toEntity(): MyListEntity = MyListEntity(
    tmdbId = tmdbId,
    mediaType = mediaType.name,
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    voteAverage = voteAverage,
    overview = overview,
    addedAt = addedAt
)

// ─── Watch Progress ──────────────────────────────────────────────────────────

@Entity(
    tableName = "watch_progress",
    indices = [Index(value = ["tmdb_id", "season_number", "episode_number"], unique = true)]
)
data class WatchProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "tmdb_id") val tmdbId: Int,
    @ColumnInfo(name = "media_type") val mediaType: String,
    val title: String,
    @ColumnInfo(name = "poster_path") val posterPath: String? = null,
    @ColumnInfo(name = "backdrop_path") val backdropPath: String? = null,
    @ColumnInfo(name = "still_path") val stillPath: String? = null,
    @ColumnInfo(name = "season_number") val seasonNumber: Int? = null,
    @ColumnInfo(name = "episode_number") val episodeNumber: Int? = null,
    @ColumnInfo(name = "episode_title") val episodeTitle: String? = null,
    @ColumnInfo(name = "progress_ms") val progressMs: Long = 0L,
    @ColumnInfo(name = "duration_ms") val durationMs: Long = 0L,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "last_watched_at") val lastWatchedAt: Long = System.currentTimeMillis()
)

fun WatchProgressEntity.toDomain(): WatchProgress = WatchProgress(
    tmdbId = tmdbId,
    mediaType = TmdbMediaType.valueOf(mediaType),
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    stillPath = stillPath,
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    episodeTitle = episodeTitle,
    progressMs = progressMs,
    durationMs = durationMs,
    isCompleted = isCompleted,
    lastWatchedAt = lastWatchedAt
)

fun WatchProgress.toEntity(): WatchProgressEntity = WatchProgressEntity(
    tmdbId = tmdbId,
    mediaType = mediaType.name,
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    stillPath = stillPath,
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    episodeTitle = episodeTitle,
    progressMs = progressMs,
    durationMs = durationMs,
    isCompleted = isCompleted,
    lastWatchedAt = lastWatchedAt
)

// ─── Episode Watched Status ──────────────────────────────────────────────────

@Entity(
    tableName = "episode_watched",
    indices = [Index(value = ["tmdb_id", "season_number", "episode_number"], unique = true)]
)
data class EpisodeWatchedEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "tmdb_id") val tmdbId: Int,
    @ColumnInfo(name = "season_number") val seasonNumber: Int,
    @ColumnInfo(name = "episode_number") val episodeNumber: Int,
    @ColumnInfo(name = "is_watched") val isWatched: Boolean = false,
    @ColumnInfo(name = "watched_at") val watchedAt: Long? = null
)

// ─── Installed Addons ────────────────────────────────────────────────────────

@Entity(tableName = "installed_addons")
data class AddonEntity(
    @PrimaryKey val id: String,
    val name: String,
    val version: String = "",
    val description: String = "",
    @ColumnInfo(name = "manifest_url") val manifestUrl: String,
    @ColumnInfo(name = "base_url") val baseUrl: String,
    val types: String = "",          // JSON array string
    val resources: String = "",      // JSON array string
    @ColumnInfo(name = "addon_type") val addonType: String = "STREAM",
    val priority: Int = 0,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = true,
    @ColumnInfo(name = "is_built_in") val isBuiltIn: Boolean = false,
    @ColumnInfo(name = "requires_debrid") val requiresDebrid: Boolean = false
)

fun AddonEntity.toDomain(): StremioAddon = StremioAddon(
    id = id,
    name = name,
    version = version,
    description = description,
    manifestUrl = manifestUrl,
    baseUrl = baseUrl,
    types = types.split(",").filter { it.isNotBlank() },
    resources = resources.split(",").filter { it.isNotBlank() },
    addonType = try { StremioAddonType.valueOf(addonType) } catch (e: Exception) { StremioAddonType.STREAM },
    priority = priority,
    isEnabled = isEnabled,
    isBuiltIn = isBuiltIn,
    requiresDebrid = requiresDebrid
)

fun StremioAddon.toEntity(): AddonEntity = AddonEntity(
    id = id,
    name = name,
    version = version,
    description = description,
    manifestUrl = manifestUrl,
    baseUrl = baseUrl,
    types = types.joinToString(","),
    resources = resources.joinToString(","),
    addonType = addonType.name,
    priority = priority,
    isEnabled = isEnabled,
    isBuiltIn = isBuiltIn,
    requiresDebrid = requiresDebrid
)
