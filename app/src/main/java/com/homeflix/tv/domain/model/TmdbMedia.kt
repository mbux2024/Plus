package com.homeflix.tv.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Core media item from TMDB. Used for catalog browsing, home rows, search results.
 * This replaces the old Media model that relied on a custom server.
 */
@Parcelize
data class TmdbMedia(
    val id: Int,
    val title: String,
    val originalTitle: String? = null,
    val mediaType: TmdbMediaType,
    val overview: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val logoPath: String? = null,
    val releaseDate: String? = null,
    val firstAirDate: String? = null,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val popularity: Double = 0.0,
    val genreIds: List<Int> = emptyList(),
    val genres: List<TmdbGenre> = emptyList(),
    val adult: Boolean = false,
    val originalLanguage: String? = null,
    // External IDs (fetched on detail page)
    val imdbId: String? = null,
    val tvdbId: Int? = null,
    // Enriched detail fields (fetched on demand)
    val tagline: String? = null,
    val runtime: Int? = null,
    val status: String? = null,
    val numberOfSeasons: Int? = null,
    val numberOfEpisodes: Int? = null,
    val homepage: String? = null,
    // Certification/content rating
    val certification: String? = null
) : Parcelable {

    // ─── Primary computed properties ─────────────────────────────────────

    val year: Int?
        get() = (releaseDate ?: firstAirDate)?.take(4)?.toIntOrNull()

    val displayTitle: String
        get() = title

    fun posterUrl(size: String = "w500"): String? =
        posterPath?.let { "https://image.tmdb.org/t/p/$size$it" }

    fun backdropUrl(size: String = "w1280"): String? =
        backdropPath?.let { "https://image.tmdb.org/t/p/$size$it" }

    fun logoUrl(size: String = "w300"): String? =
        logoPath?.let { "https://image.tmdb.org/t/p/$size$it" }

    // ─── Backward-compat properties (used by existing UI composables) ────

    /** Old UI: media.rating (maps to voteAverage) */
    val rating: Double get() = voteAverage

    /** Old UI: media.description (maps to overview) */
    val description: String? get() = overview

    /** Old UI: media.genreNames as List<String> */
    val genreNames: List<String> get() = genres.map { it.name }

    /** Old UI: media.quality — not available from TMDB */
    val quality: String? get() = null

    /** Old UI: media.type as MediaType enum */
    val type: MediaType get() = MediaType.from(mediaType)

    /** Old UI: media.tmdbBackdropUrl */
    val tmdbBackdropUrl: String? get() = backdropUrl()

    /** Old UI: media.tmdbPosterUrl */
    val tmdbPosterUrl: String? get() = posterUrl()

    /** Old UI: media.bannerPath — maps to backdropPath */
    val bannerPath: String? get() = backdropPath

    /** Old UI: media.previewPath — not available */
    val previewPath: String? get() = null

    /** Old UI: media.filePath — not applicable */
    val filePath: String get() = ""

    /** Old UI: media.viewCount */
    val viewCount: Int get() = 0

    /** Old UI: media.duration (seconds) — maps to runtime (minutes) * 60 */
    val duration: Int get() = (runtime ?: 0) * 60
}

enum class TmdbMediaType {
    MOVIE,
    TV;

    val stremioType: String
        get() = when (this) {
            MOVIE -> "movie"
            TV -> "series"
        }
}

@Parcelize
data class TmdbGenre(
    val id: Int,
    val name: String
) : Parcelable

@Parcelize
data class TmdbCastMember(
    val id: Int,
    val name: String,
    val character: String? = null,
    val profilePath: String? = null,
    val order: Int = 0,
    val department: String? = null,
    val job: String? = null // "Director", "Writer", etc.
) : Parcelable {
    fun profileUrl(size: String = "w185"): String? =
        profilePath?.let { "https://image.tmdb.org/t/p/$size$it" }
}

@Parcelize
data class TmdbCredits(
    val cast: List<TmdbCastMember> = emptyList(),
    val crew: List<TmdbCastMember> = emptyList()
) : Parcelable {
    val directors: List<TmdbCastMember>
        get() = crew.filter { it.job == "Director" }
    val writers: List<TmdbCastMember>
        get() = crew.filter { it.department == "Writing" }
}

@Parcelize
data class TmdbVideo(
    val id: String,
    val key: String, // YouTube video ID
    val name: String,
    val site: String, // "YouTube"
    val type: String, // "Trailer", "Teaser", etc.
    val official: Boolean = false
) : Parcelable

@Parcelize
data class TmdbSeason(
    val id: Int,
    val seasonNumber: Int,
    val name: String,
    val overview: String? = null,
    val posterPath: String? = null,
    val airDate: String? = null,
    val episodeCount: Int = 0
) : Parcelable {
    fun posterUrl(size: String = "w300"): String? =
        posterPath?.let { "https://image.tmdb.org/t/p/$size$it" }
}

@Parcelize
data class TmdbEpisode(
    val id: Int,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val name: String,
    val overview: String? = null,
    val stillPath: String? = null,
    val airDate: String? = null,
    val runtime: Int? = null,
    val voteAverage: Double = 0.0
) : Parcelable {
    fun stillUrl(size: String = "w300"): String? =
        stillPath?.let { "https://image.tmdb.org/t/p/$size$it" }
}

/**
 * Full detail for a media item — includes credits, videos, seasons, external IDs.
 * Loaded when navigating to the detail page.
 */
@Parcelize
data class TmdbMediaDetail(
    val media: TmdbMedia,
    val credits: TmdbCredits = TmdbCredits(),
    val videos: List<TmdbVideo> = emptyList(),
    val seasons: List<TmdbSeason> = emptyList(),
    val similar: List<TmdbMedia> = emptyList(),
    val collection: TmdbCollection? = null
) : Parcelable {
    val trailer: TmdbVideo?
        get() = videos.firstOrNull { it.type == "Trailer" && it.site == "YouTube" }
            ?: videos.firstOrNull { it.site == "YouTube" }
}

@Parcelize
data class TmdbCollection(
    val id: Int,
    val name: String,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val parts: List<TmdbMedia> = emptyList()
) : Parcelable

/**
 * Watch provider info from TMDB (Netflix, Disney+, etc.)
 */
@Parcelize
data class TmdbWatchProvider(
    val providerId: Int,
    val providerName: String,
    val logoPath: String? = null,
    val displayPriority: Int = 0
) : Parcelable {
    fun logoUrl(size: String = "w92"): String? =
        logoPath?.let { "https://image.tmdb.org/t/p/$size$it" }
}
