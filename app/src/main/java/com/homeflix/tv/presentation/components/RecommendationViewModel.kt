package com.homeflix.tv.presentation.components

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.homeflix.tv.data.model.CatalogItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

sealed class RecommendationUiState {
    object Loading : RecommendationUiState()
    data class Success(val personalizedRecommendations: List<CatalogItem> = emptyList(), val similarRecommendations: List<CatalogItem> = emptyList(), val genreRecommendations: List<CatalogItem> = emptyList(), val trendingRecommendations: List<CatalogItem> = emptyList(), val topRatedRecommendations: List<CatalogItem> = emptyList(), val mixedRecommendations: List<CatalogItem> = emptyList()) : RecommendationUiState()
    data class Error(val message: String) : RecommendationUiState()
}

@HiltViewModel
class RecommendationViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Loading)
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()
    fun loadRecommendations(currentMedia: Any) { _uiState.value = RecommendationUiState.Success() }
}
