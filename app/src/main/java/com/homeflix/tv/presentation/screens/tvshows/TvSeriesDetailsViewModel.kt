package com.homeflix.tv.presentation.screens.tvshows

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TvSeriesDetailsViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<TvSeriesDetailsUiState>(TvSeriesDetailsUiState.Loading)
    val uiState: StateFlow<TvSeriesDetailsUiState> = _uiState.asStateFlow()
    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()
    private val _selectedSeason = MutableStateFlow(1)
    val selectedSeason: StateFlow<Int> = _selectedSeason.asStateFlow()
    private val _episodesLoading = MutableStateFlow(false)
    val episodesLoading: StateFlow<Boolean> = _episodesLoading.asStateFlow()
    fun loadSeriesDetails(seriesId: String) { _uiState.value = TvSeriesDetailsUiState.Success(series = TvSeries(id = seriesId.toIntOrNull() ?: 0, title = "Loading..."), seasons = emptyList()) }
    fun selectSeason(seriesId: String, seasonNumber: Int) { _selectedSeason.value = seasonNumber }
}
sealed class TvSeriesDetailsUiState {
    object Loading : TvSeriesDetailsUiState()
    data class Success(val series: TvSeries, val seasons: List<Season> = emptyList()) : TvSeriesDetailsUiState()
    data class Error(val message: String) : TvSeriesDetailsUiState()
}
