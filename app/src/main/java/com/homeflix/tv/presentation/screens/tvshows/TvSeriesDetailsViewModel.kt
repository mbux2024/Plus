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
class TvSeriesDetailsViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TvSeriesDetailsUiState>(TvSeriesDetailsUiState.Loading)
    val uiState: StateFlow<TvSeriesDetailsUiState> = _uiState.asStateFlow()

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()

    private val _selectedSeason = MutableStateFlow(1)
    val selectedSeason: StateFlow<Int> = _selectedSeason.asStateFlow()

    private val _episodesLoading = MutableStateFlow(false)
    val episodesLoading: StateFlow<Boolean> = _episodesLoading.asStateFlow()

    fun loadSeriesDetails(seriesId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = TvSeriesDetailsUiState.Loading
                val tvId = seriesId.toIntOrNull() ?: return@launch

                tmdbRepository.getTvDetails(tvId).fold(
                    onSuccess = { detail ->
                        _uiState.value = TvSeriesDetailsUiState.Success(detail = detail)
                        // Auto-load first season episodes
                        detail.seasons.firstOrNull()?.let {
                            selectSeason(seriesId, it.seasonNumber)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = TvSeriesDetailsUiState.Error(
                            message = error.message ?: "Failed to load series details"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = TvSeriesDetailsUiState.Error(
                    message = e.message ?: "Failed to load series details"
                )
            }
        }
    }

    fun selectSeason(seriesId: String, seasonNumber: Int) {
        _selectedSeason.value = seasonNumber
        viewModelScope.launch {
            _episodesLoading.value = true
            try {
                val tvId = seriesId.toIntOrNull() ?: return@launch
                tmdbRepository.getSeasonDetails(tvId, seasonNumber).fold(
                    onSuccess = { episodeList ->
                        _episodes.value = episodeList.map { ep ->
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
                    },
                    onFailure = { _episodes.value = emptyList() }
                )
            } catch (e: Exception) {
                _episodes.value = emptyList()
            } finally {
                _episodesLoading.value = false
            }
        }
    }
}

sealed class TvSeriesDetailsUiState {
    object Loading : TvSeriesDetailsUiState()
    data class Success(
        val detail: TmdbMediaDetail,
        // Backward compat: old TvSeriesDetailsScreen accesses state.series and state.seasons
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
        val seasons: List<com.homeflix.tv.presentation.screens.tvshows.Season> = detail.seasons.map { s ->
            com.homeflix.tv.presentation.screens.tvshows.Season(
                id = s.id,
                seasonNumber = s.seasonNumber,
                name = s.name,
                overview = s.overview,
                posterPath = s.posterPath,
                airDate = s.airDate,
                episodeCount = s.episodeCount
            )
        }
    ) : TvSeriesDetailsUiState()
    data class Error(val message: String) : TvSeriesDetailsUiState()
}
