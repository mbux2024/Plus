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
                val episodes = episodesResult.getOrDefault(emptyList())

                if (detail != null) {
                    _uiState.value = TvSeriesSeasonUiState.Success(
                        detail = detail,
                        seasonNumber = seasonNumber,
                        episodes = episodes
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
        val episodes: List<TmdbEpisode>
    ) : TvSeriesSeasonUiState()
    data class Error(val message: String) : TvSeriesSeasonUiState()
}
