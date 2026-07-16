package com.homeflix.tv.presentation.screens.tvshows

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homeflix.tv.HomeFlixTVApplication
import com.homeflix.tv.data.model.CatalogItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvShowsViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val tmdb get() = (getApplication<HomeFlixTVApplication>()).container.tmdbRepository
    private val _uiState = MutableStateFlow<TvShowsUiState>(TvShowsUiState.Loading)
    val uiState: StateFlow<TvShowsUiState> = _uiState.asStateFlow()
    init { loadTvShows() }
    fun loadTvShows() { viewModelScope.launch { try {
        val tv = tmdb.trendingTv()
        _uiState.value = TvShowsUiState.Success(featuredSeries = emptyList(), series = emptyList(), continueWatchingEpisodes = emptyList())
    } catch (e: Exception) { _uiState.value = TvShowsUiState.Error(e.message ?: "Error") }}}
    fun refreshContinueWatching() {}
}
sealed class TvShowsUiState {
    object Loading : TvShowsUiState()
    data class Success(val featuredSeries: List<TvSeries> = emptyList(), val series: List<TvSeries> = emptyList(), val continueWatchingEpisodes: List<com.homeflix.tv.presentation.components.ContinueWatchingItem> = emptyList()) : TvShowsUiState()
    data class Error(val message: String) : TvShowsUiState()
}
data class TvSeries(val id: Int = 0, val title: String = "", val description: String? = null, val rating: Double = 0.0, val year: Int? = null, val totalSeasons: Int = 0, val totalEpisodes: Int = 0, val genres: List<String> = emptyList(), val posterPath: String? = null, val bannerPath: String? = null, val tmdbPosterUrl: String? = null, val tmdbBackdropUrl: String? = null, val createdAt: String? = null)
data class Season(val id: Int = 0, val seasonNumber: Int = 1, val name: String = "", val overview: String? = null, val description: String? = overview, val posterPath: String? = null, val airDate: String? = null, val episodeCount: Int = 0)
data class Episode(val id: Int = 0, val title: String = "", val episodeTitle: String? = null, val description: String? = null, val duration: Int? = null, val rating: Double = 0.0, val airDate: String? = null, val thumbnailPath: String? = null, val episodeStillPath: String? = null)
