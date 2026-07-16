package com.homeflix.tv.presentation.screens.tvshows

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflix.tv.domain.model.TmdbMedia
import com.homeflix.tv.domain.model.TmdbMediaType
import com.homeflix.tv.domain.repository.TmdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvShowsViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TvShowsUiState>(TvShowsUiState.Loading)
    val uiState: StateFlow<TvShowsUiState> = _uiState.asStateFlow()

    init {
        loadTvShows()
    }

    fun loadTvShows() {
        viewModelScope.launch {
            try {
                _uiState.value = TvShowsUiState.Loading

                val trendingTv = tmdbRepository.getTrendingTv().getOrDefault(emptyList())
                val popularTv = tmdbRepository.getPopularTv().getOrDefault(emptyList())
                val topRatedTv = tmdbRepository.getTopRatedTv().getOrDefault(emptyList())
                val airingToday = tmdbRepository.getAiringTodayTv().getOrDefault(emptyList())

                _uiState.value = TvShowsUiState.Success(
                    featuredShows = trendingTv.take(5),
                    trendingTv = trendingTv,
                    popularTv = popularTv,
                    topRatedTv = topRatedTv,
                    airingToday = airingToday
                )
            } catch (e: Exception) {
                Log.e("TvShowsViewModel", "Error loading TV shows", e)
                _uiState.value = TvShowsUiState.Error(
                    message = e.message ?: "Failed to load TV shows"
                )
            }
        }
    }
}

sealed class TvShowsUiState {
    object Loading : TvShowsUiState()
    data class Success(
        val featuredShows: List<TmdbMedia>,
        val trendingTv: List<TmdbMedia>,
        val popularTv: List<TmdbMedia>,
        val topRatedTv: List<TmdbMedia>,
        val airingToday: List<TmdbMedia>
    ) : TvShowsUiState()
    data class Error(val message: String) : TvShowsUiState()
}

// Keep legacy data classes for any screens that still reference them
data class TvSeries(
    val id: Int,
    val title: String,
    val description: String?,
    val rating: Double,
    val year: Int?,
    val totalSeasons: Int,
    val totalEpisodes: Int,
    val genres: List<String>,
    val posterPath: String?,
    val bannerPath: String?,
    val tmdbPosterUrl: String? = null,
    val tmdbBackdropUrl: String? = null,
    val createdAt: String? = null
)

data class Season(
    val id: Int,
    val seasonNumber: Int,
    val name: String,
    val overview: String? = null,
    val posterPath: String? = null,
    val airDate: String? = null,
    val episodeCount: Int = 0
)

data class Episode(
    val id: Int,
    val title: String,
    val episodeTitle: String? = null,
    val description: String?,
    val duration: Int?,
    val rating: Double,
    val airDate: String?,
    val thumbnailPath: String?,
    val episodeStillPath: String? = null
)
