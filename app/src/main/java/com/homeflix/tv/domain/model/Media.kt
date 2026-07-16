@file:Suppress("unused")
package com.homeflix.tv.domain.model

/**
 * COMPATIBILITY LAYER
 * 
 * This file provides backward-compatible type aliases and extension properties
 * so existing UI composables (CinematicHero, MediaCard, MediaRow, etc.) compile
 * without modification. All UI code that used the old `Media` class now transparently
 * uses `TmdbMedia` via this alias.
 *
 * The old Media was a Parcelable with fields like: id, title, rating, year, genres,
 * posterPath, bannerPath, tmdbBackdropUrl, tmdbPosterUrl, description, genreNames,
 * certification, quality, runtime, type (MOVIE/EPISODE/TV_SHOW), filePath, etc.
 */

// Type alias: old `Media` = new `TmdbMedia`
typealias Media = TmdbMedia

// Type alias: old `MediaType` = new enum
// The old enum had MOVIE, EPISODE, TV_SHOW — map to TmdbMediaType
enum class MediaType {
    MOVIE,
    EPISODE,
    TV_SHOW;

    companion object {
        fun from(tmdbType: TmdbMediaType): MediaType = when (tmdbType) {
            TmdbMediaType.MOVIE -> MOVIE
            TmdbMediaType.TV -> TV_SHOW
        }
    }
}

// Type alias: old `Genre` = new `TmdbGenre`
typealias Genre = TmdbGenre

// ─── Extension properties to satisfy old field access patterns ────────────────

/** Old UI accessed media.rating — map to voteAverage */
val TmdbMedia.rating: Double get() = voteAverage

/** Old UI accessed media.description — map to overview */
val TmdbMedia.description: String? get() = overview

/** Old UI accessed media.genreNames as List<String> */
val TmdbMedia.genreNames: List<String> get() = genres.map { it.name }

/** Old UI accessed media.certification */
val TmdbMedia.certificationCompat: String? get() = certification

/** Old UI accessed media.quality — not available from TMDB, return null */
val TmdbMedia.quality: String? get() = null

/** Old UI accessed media.type as MediaType enum */
val TmdbMedia.type: MediaType get() = MediaType.from(mediaType)

/** Old UI accessed media.tmdbBackdropUrl */
val TmdbMedia.tmdbBackdropUrl: String? get() = backdropUrl()

/** Old UI accessed media.tmdbPosterUrl */
val TmdbMedia.tmdbPosterUrl: String? get() = posterUrl()

/** Old UI accessed media.bannerPath — map to backdrop */
val TmdbMedia.bannerPath: String? get() = backdropPath

/** Old UI accessed media.previewPath — not available */
val TmdbMedia.previewPath: String? get() = null

/** Old UI accessed media.filePath — not applicable in TMDB model */
val TmdbMedia.filePath: String get() = ""

/** Old UI accessed media.viewCount */
val TmdbMedia.viewCount: Int get() = 0

/** Old UI accessed media.duration (seconds) — map to runtime (minutes) */
val TmdbMedia.duration: Int get() = (runtime ?: 0) * 60
