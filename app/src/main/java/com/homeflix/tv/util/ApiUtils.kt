package com.homeflix.tv.util

import com.homeflix.tv.domain.model.TmdbMedia
import com.homeflix.tv.presentation.screens.tvshows.TvSeries

/**
 * COMPATIBILITY LAYER — ApiUtils
 *
 * Provides image URL construction for all existing UI composables.
 * Now uses TMDB image CDN instead of the old custom server.
 */
object ApiUtils {

    private const val TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p"

    // ─── Base URL (referenced by VideoPlayer and TvShows screens) ─────────

    fun getBaseUrl(): String = TMDB_IMAGE_BASE

    // ─── Media Image URLs (return empty string when path is null) ─────────

    fun getBackdropUrl(media: TmdbMedia): String =
        media.backdropPath?.let { "$TMDB_IMAGE_BASE/w1280$it" } ?: ""

    fun getPosterUrl(media: TmdbMedia): String =
        media.posterPath?.let { "$TMDB_IMAGE_BASE/w500$it" } ?: ""

    fun getBannerUrl(media: TmdbMedia): String =
        media.backdropPath?.let { "$TMDB_IMAGE_BASE/w780$it" } ?: ""

    fun getThumbnailUrl(media: TmdbMedia): String =
        media.posterPath?.let { "$TMDB_IMAGE_BASE/w342$it" } ?: ""

    fun getLogoUrl(media: TmdbMedia): String? =
        media.logoPath?.let { "$TMDB_IMAGE_BASE/w300$it" }

    /**
     * Preview clip URL — not available via TMDB.
     * Returns null; BackgroundVideo composable should fall back to still image.
     */
    fun getPreviewClipUrl(mediaId: Int): String? = null

    /**
     * Subtitle URL — subtitles are now fetched via Stremio addon.
     */
    fun getSubtitleUrl(mediaId: Int, trackId: Int): String = ""

    // ─── TV Series URLs ──────────────────────────────────────────────────

    fun getSeriesBackdropUrl(series: TvSeries): String {
        return series.tmdbBackdropUrl
            ?: series.bannerPath?.let { path ->
                if (path.startsWith("http")) path
                else "$TMDB_IMAGE_BASE/w1280$path"
            }
            ?: series.posterPath?.let { path ->
                if (path.startsWith("http")) path
                else "$TMDB_IMAGE_BASE/w780$path"
            }
            ?: ""
    }

    fun getSeriesPosterUrl(series: TvSeries): String {
        return series.tmdbPosterUrl
            ?: series.posterPath?.let { path ->
                if (path.startsWith("http")) path
                else "$TMDB_IMAGE_BASE/w500$path"
            }
            ?: ""
    }

    fun getSeriesLogoUrl(seriesId: Int): String? {
        // Logo not available without a separate TMDB images API call
        return null
    }

    /** Episode thumbnail — used by TvSeriesDetailsScreen */
    fun getEpisodeThumbnailUrl(episode: com.homeflix.tv.presentation.screens.tvshows.Episode): String {
        return episode.episodeStillPath
            ?: episode.thumbnailPath
            ?: ""
    }
}
