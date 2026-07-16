package com.homeflix.tv.presentation.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflix.tv.domain.model.TmdbMedia
import com.homeflix.tv.domain.repository.TmdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RecommendationUiState {
    object Loading : RecommendationUiState()
    data class Success(
        val similarRecommendations: List<TmdbMedia> = emptyList(),
        val trendingRecommendations: List<TmdbMedia> = emptyList(),
        val topRatedRecommendations: List<TmdbMedia> = emptyList()
    ) : RecommendationUiState()
    data class Error(val message: String) : RecommendationUiState()
}

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Loading)
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    fun loadRecommendations(tmdbId: Int, mediaType: com.homeflix.tv.domain.model.TmdbMediaType) {
        viewModelScope.launch {
            try {
                _uiState.value = RecommendationUiState.Loading

                val similar = tmdbRepository.getSimilar(tmdbId, mediaType).getOrDefault(emptyList())
                val trending = tmdbRepository.getTrendingMovies().getOrDefault(emptyList())
                val topRated = tmdbRepository.getTopRatedMovies().getOrDefault(emptyList())

                _uiState.value = RecommendationUiState.Success(
                    similarRecommendations = similar.take(20),
                    trendingRecommendations = trending.take(20),
                    topRatedRecommendations = topRated.take(20)
                )
            } catch (e: Exception) {
                _uiState.value = RecommendationUiState.Error(
                    message = e.message ?: "Failed to load recommendations"
                )
            }
        }
    }
}
