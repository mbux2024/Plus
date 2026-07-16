package com.homeflix.tv.util

import com.homeflix.tv.data.model.CatalogItem
import com.homeflix.tv.presentation.screens.tvshows.TvSeries

/**
 * Compatibility layer — provides image URL helpers for existing UI components.
 * Uses CatalogItem fields (posterUrl, backdropUrl) which are already full URLs.
 */
object ApiUtils {
    fun getBaseUrl(): String = "https://image.tmdb.org/t/p"
    fun getBackdropUrl(media: CatalogItem): String = media.backdropUrl ?: ""
    fun getPosterUrl(media: CatalogItem): String = media.posterUrl ?: ""
    fun getBannerUrl(media: CatalogItem): String = media.backdropUrl ?: ""
    fun getThumbnailUrl(media: CatalogItem): String = media.posterUrl ?: ""
    fun getLogoUrl(media: CatalogItem): String? = null
    fun getPreviewClipUrl(mediaId: Int): String? = null
    fun getSubtitleUrl(mediaId: Int, trackId: Int): String = ""
    fun getSeriesBackdropUrl(series: TvSeries): String = series.tmdbBackdropUrl ?: series.bannerPath ?: ""
    fun getSeriesPosterUrl(series: TvSeries): String = series.tmdbPosterUrl ?: series.posterPath ?: ""
    fun getSeriesLogoUrl(seriesId: Int): String? = null
    fun getEpisodeThumbnailUrl(episode: com.homeflix.tv.presentation.screens.tvshows.Episode): String = episode.episodeStillPath ?: episode.thumbnailPath ?: ""
}
