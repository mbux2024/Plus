package com.homeflix.tv.presentation.screens.tvshows

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TvSeriesSeasonViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<TvSeriesSeasonUiState>(TvSeriesSeasonUiState.Loading)
    val uiState: StateFlow<TvSeriesSeasonUiState> = _uiState.asStateFlow()
    fun loadSeasonDetails(seriesId: String, seasonNumber: Int) { _uiState.value = TvSeriesSeasonUiState.Success(series = TvSeries(id = seriesId.toIntOrNull() ?: 0, title = "Loading..."), season = Season(seasonNumber = seasonNumber, name = "Season $seasonNumber"), episodes = emptyList()) }
}
sealed class TvSeriesSeasonUiState {
    object Loading : TvSeriesSeasonUiState()
    data class Success(val series: TvSeries, val season: Season, val episodes: List<Episode> = emptyList()) : TvSeriesSeasonUiState()
    data class Error(val message: String) : TvSeriesSeasonUiState()
}
