package com.homeflix.tv.data.model

import com.homeflix.tv.data.tmdb.TmdbResult
import kotlinx.serialization.Serializable

/** Whether a piece of content is a movie or a TV series. */
@Serializable
enum class MediaType(val tmdb: String) {
    MOVIE("movie"),
    TV("tv");

    companion object {
        fun from(raw: String?): MediaType =
            if (raw.equals("tv", ignoreCase = true)) TV else MOVIE
    }
}

/**
 * Lightweight, UI-friendly representation of a catalog item used across rows,
 * cards and detail navigation. Built from a [TmdbResult].
 */
@Serializable
data class CatalogItem(
    val id: Int,
    val type: MediaType,
    val title: String,
    val overview: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val rating: Double,
    val year: String?,
    /** TMDB genre ids from the list response, used for the Netflix-style meta line. */
    val genreIds: List<Int> = emptyList()
)

/** A named cast/crew person for the detail-page Cast row. */
data class CastPerson(
    val id: Int,
    val name: String,
    val role: String,
    val imageUrl: String?
)

/** A named horizontal row on the home screen. */
data class CatalogRow(
    val title: String,
    val items: List<CatalogItem>,
    val ranked: Boolean = false
)

/** Backward compat: old UI uses "Media" as a type name. */
typealias Media = CatalogItem

// ─── Extension properties for backward compatibility with old UI composables ───
// CinematicHero, MediaCard, ContinueWatchingRow etc. access these fields.

/** Old UI: media.description — maps to overview */
val CatalogItem.description: String? get() = overview

/** Old UI: media.genreNames — not available from list API, return empty */
val CatalogItem.genreNames: List<String> get() = emptyList()

/** Old UI: media.runtime — not available from list API */
val CatalogItem.runtime: Int? get() = null

/** Old UI: media.certification — not available from list API */
val CatalogItem.certification: String? get() = null

/** Old UI: media.quality — not applicable */
val CatalogItem.quality: String? get() = null

/** Old UI: media.bannerPath — maps to backdropUrl */
val CatalogItem.bannerPath: String? get() = backdropUrl

/** Old UI: media.tmdbBackdropUrl */
val CatalogItem.tmdbBackdropUrl: String? get() = backdropUrl

/** Old UI: media.tmdbPosterUrl */
val CatalogItem.tmdbPosterUrl: String? get() = posterUrl

/** Old UI: media.posterPath */
val CatalogItem.posterPath: String? get() = posterUrl

/** Old UI: media.backdropPath */
val CatalogItem.backdropPath: String? get() = backdropUrl

/** Old UI: media.previewPath */
val CatalogItem.previewPath: String? get() = null

/** Old UI: media.filePath */
val CatalogItem.filePath: String get() = ""

/** Old UI: media.viewCount */
val CatalogItem.viewCount: Int get() = 0

/** Old UI: media.duration (seconds) */
val CatalogItem.duration: Int get() = 0

/** Old UI: media.seriesId */
val CatalogItem.seriesId: Int? get() = null

/** Old UI: media.seasonNumber */
val CatalogItem.seasonNumber: Int? get() = null

/** Old UI: media.episodeNumber */
val CatalogItem.episodeNumber: Int? get() = null

