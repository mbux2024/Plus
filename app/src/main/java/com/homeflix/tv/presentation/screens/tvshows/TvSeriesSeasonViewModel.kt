package com.homeflix.tv.presentation.screens.tvshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflix.tv.domain.model.*
import com.homeflix.tv.domain.repository.TmdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvSeriesSeasonViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TvSeriesSeasonUiState>(TvSeriesSeasonUiState.Loading)
    val uiState: StateFlow<TvSeriesSeasonUiState> = _uiState.asStateFlow()

    fun loadSeasonDetails(seriesId: String, seasonNumber: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = TvSeriesSeasonUiState.Loading
                val tvId = seriesId.toIntOrNull() ?: return@launch

                val detailResult = tmdbRepository.getTvDetails(tvId)
                val episodesResult = tmdbRepository.getSeasonDetails(tvId, seasonNumber)

                val detail = detailResult.getOrNull()
                val tmdbEpisodes = episodesResult.getOrDefault(emptyList())

                // Convert TmdbEpisode → Episode for screen compatibility
                val episodes = tmdbEpisodes.map { ep ->
                    Episode(
                        id = ep.id,
                        title = ep.name,
                        episodeTitle = ep.name,
                        description = ep.overview,
                        duration = ep.runtime,
                        rating = ep.voteAverage,
                        airDate = ep.airDate,
                        thumbnailPath = ep.stillPath?.let { "https://image.tmdb.org/t/p/w300$it" },
                        episodeStillPath = ep.stillPath?.let { "https://image.tmdb.org/t/p/w300$it" }
                    )
                }

                if (detail != null) {
                    _uiState.value = TvSeriesSeasonUiState.Success(
                        detail = detail,
                        seasonNumber = seasonNumber,
                        episodesList = episodes
                    )
                } else {
                    _uiState.value = TvSeriesSeasonUiState.Error("Failed to load series")
                }
            } catch (e: Exception) {
                _uiState.value = TvSeriesSeasonUiState.Error(
                    message = e.message ?: "Failed to load season details"
                )
            }
        }
    }
}

sealed class TvSeriesSeasonUiState {
    object Loading : TvSeriesSeasonUiState()
    data class Success(
        val detail: TmdbMediaDetail,
        val seasonNumber: Int,
        val episodesList: List<Episode>,
        // Backward compat
        val series: com.homeflix.tv.presentation.screens.tvshows.TvSeries = detail.media.let { media ->
            com.homeflix.tv.presentation.screens.tvshows.TvSeries(
                id = media.id,
                title = media.title,
                description = media.overview,
                rating = media.voteAverage,
                year = media.year,
                totalSeasons = media.numberOfSeasons ?: 0,
                totalEpisodes = media.numberOfEpisodes ?: 0,
                genres = media.genres.map { it.name },
                posterPath = media.posterPath,
                bannerPath = media.backdropPath,
                tmdbPosterUrl = media.posterUrl(),
                tmdbBackdropUrl = media.backdropUrl()
            )
        },
        val season: com.homeflix.tv.presentation.screens.tvshows.Season = com.homeflix.tv.presentation.screens.tvshows.Season(
            id = 0,
            seasonNumber = seasonNumber,
            name = "Season $seasonNumber",
            episodeCount = episodesList.size
        ),
        val episodes: List<Episode> = episodesList
    ) : TvSeriesSeasonUiState()
    data class Error(val message: String) : TvSeriesSeasonUiState()
}
