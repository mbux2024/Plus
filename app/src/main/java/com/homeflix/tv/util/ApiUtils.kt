package com.homeflix.tv.util

import com.homeflix.tv.domain.model.TmdbMedia
import com.homeflix.tv.presentation.screens.tvshows.TvSeries

/**
 * COMPATIBILITY LAYER — ApiUtils
 *
 * The old ApiUtils built image URLs from the custom HomeFlixAPI server.
 * This replacement uses TMDB image URLs directly from the TmdbMedia model.
 * 
 * All the UI components (CinematicHero, MediaCard, ContinueWatchingRow, etc.)
 * call these functions to get image URLs for Coil/AsyncImage loading.
 */
object ApiUtils {

    private const val TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p"

    // ─── Base URL (no longer needed but referenced in some places) ────────

    fun getBaseUrl(): String = TMDB_IMAGE_BASE

    // ─── Media Image URLs ────────────────────────────────────────────────

    fun getBackdropUrl(media: TmdbMedia): String? =
        media.backdropPath?.let { "$TMDB_IMAGE_BASE/w1280$it" }

    fun getPosterUrl(media: TmdbMedia): String? =
        media.posterPath?.let { "$TMDB_IMAGE_BASE/w500$it" }

    fun getBannerUrl(media: TmdbMedia): String? =
        media.backdropPath?.let { "$TMDB_IMAGE_BASE/w780$it" }

    fun getThumbnailUrl(media: TmdbMedia): String? =
        media.posterPath?.let { "$TMDB_IMAGE_BASE/w342$it" }

    fun getLogoUrl(media: TmdbMedia): String? =
        media.logoPath?.let { "$TMDB_IMAGE_BASE/w300$it" }

    /**
     * Preview clip URL — not available via TMDB.
     * Returns null; BackgroundVideo composable should fall back to still image.
     */
    fun getPreviewClipUrl(mediaId: Int): String? = null

    /**
     * Subtitle URL — subtitles are now fetched via Stremio addon.
     * This returns a placeholder; the player handles subtitles differently now.
     */
    fun getSubtitleUrl(mediaId: Int, trackId: Int): String = ""

    // ─── TV Series URLs (TvSeries data class from TvShowsViewModel) ──────

    fun getSeriesBackdropUrl(series: TvSeries): String? {
        // Try tmdbBackdropUrl first, then construct from posterPath/bannerPath
        return series.tmdbBackdropUrl
            ?: series.bannerPath?.let { path ->
                if (path.startsWith("http")) path
                else "$TMDB_IMAGE_BASE/w1280$path"
            }
            ?: series.posterPath?.let { path ->
                if (path.startsWith("http")) path
                else "$TMDB_IMAGE_BASE/w780$path"
            }
    }

    fun getSeriesPosterUrl(series: TvSeries): String? {
        return series.tmdbPosterUrl
            ?: series.posterPath?.let { path ->
                if (path.startsWith("http")) path
                else "$TMDB_IMAGE_BASE/w500$path"
            }
    }

    fun getSeriesLogoUrl(seriesId: Int): String? {
        // Logo not available without a separate TMDB images API call
        // Return null — UI should fall back to text title
        return null
    }
}
