package com.homeflix.tv.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * My List item (saved locally on device).
 */
@Parcelize
data class MyListItem(
    val tmdbId: Int,
    val mediaType: TmdbMediaType,
    val title: String,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val voteAverage: Double = 0.0,
    val overview: String? = null,
    val addedAt: Long = System.currentTimeMillis()
) : Parcelable

/**
 * Watch progress for a single movie or episode.
 * Stored locally for resume/continue-watching functionality.
 */
@Parcelize
data class WatchProgress(
    val tmdbId: Int,               // TMDB movie or TV show ID
    val mediaType: TmdbMediaType,
    val title: String,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val stillPath: String? = null,  // Episode still
    // Episode-specific
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    // Progress
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val isCompleted: Boolean = false,
    val lastWatchedAt: Long = System.currentTimeMillis()
) : Parcelable {

    val progressPercent: Float
        get() = if (durationMs > 0) (progressMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val displayTitle: String
        get() = if (seasonNumber != null && episodeNumber != null) {
            "$title - S${seasonNumber}E${episodeNumber}"
        } else {
            title
        }

    val timeRemainingFormatted: String
        get() {
            val remaining = (durationMs - progressMs).coerceAtLeast(0) / 1000
            val hours = remaining / 3600
            val minutes = (remaining % 3600) / 60
            return if (hours > 0) "${hours}h ${minutes}m left" else "${minutes}m left"
        }
}

/**
 * Episode watched status (for marking individual episodes watched/unwatched).
 */
@Parcelize
data class EpisodeWatchedStatus(
    val tmdbId: Int,          // TV show TMDB ID
    val seasonNumber: Int,
    val episodeNumber: Int,
    val isWatched: Boolean = false,
    val watchedAt: Long? = null
) : Parcelable
