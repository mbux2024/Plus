package com.homeflix.tv.domain.model

/**
 * COMPATIBILITY LAYER
 *
 * Type aliases so existing UI composables compile against the new TmdbMedia model.
 * The backward-compat properties (rating, description, genreNames, quality, type, etc.)
 * are defined directly in TmdbMedia as computed properties.
 */

// Type alias: old `Media` → new `TmdbMedia`
typealias Media = TmdbMedia

// Type alias: old `Genre` → new `TmdbGenre`
typealias Genre = TmdbGenre

// Old MediaType enum (MOVIE, EPISODE, TV_SHOW)
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
