package com.homeflix.tv.presentation.screens.browse

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
class BrowseViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val tmdb get() = (getApplication<HomeFlixTVApplication>()).container.tmdbRepository
    private val _uiState = MutableStateFlow<BrowseUiState>(BrowseUiState.Loading)
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()
    init { loadBrowseContent() }
    fun loadBrowseContent() { viewModelScope.launch {
        _uiState.value = BrowseUiState.Loading
        try {
            val movies = tmdb.topRatedMovies()
            _uiState.value = BrowseUiState.Success(movies = movies, hasMore = false)
        } catch (e: Exception) { _uiState.value = BrowseUiState.Error(e.message ?: "Error") }
    }}
    fun loadMoreMovies() {}
}
sealed class BrowseUiState {
    object Loading : BrowseUiState()
    data class Error(val message: String) : BrowseUiState()
    data class Success(val movies: List<CatalogItem> = emptyList(), val hasMore: Boolean = false, val isLoadingMore: Boolean = false, val totalCount: Int = 0) : BrowseUiState()
}
