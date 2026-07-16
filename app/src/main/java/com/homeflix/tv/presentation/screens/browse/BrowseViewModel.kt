package com.homeflix.tv.presentation.screens.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflix.tv.domain.model.TmdbMedia
import com.homeflix.tv.domain.model.TmdbMediaType
import com.homeflix.tv.domain.repository.TmdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BROWSE SCREEN - PAGINATED TMDB CONTENT
 * Shows popular movies with pagination via TMDB API pages.
 */
@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BrowseUiState>(BrowseUiState.Loading)
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var isLoadingMore = false
    private var hasMoreContent = true
    private val allLoadedMovies = mutableListOf<TmdbMedia>()

    init {
        loadBrowseContent()
    }

    fun loadBrowseContent() {
        viewModelScope.launch {
            _uiState.value = BrowseUiState.Loading
            currentPage = 1
            allLoadedMovies.clear()
            hasMoreContent = true
            loadMoviesPage()
        }
    }

    private suspend fun loadMoviesPage() {
        try {
            tmdbRepository.getPopularMovies(page = currentPage).fold(
                onSuccess = { newMovies ->
                    if (currentPage == 1) {
                        allLoadedMovies.clear()
                    }
                    allLoadedMovies.addAll(newMovies)
                    hasMoreContent = newMovies.size >= 20 // TMDB pages have 20 items

                    _uiState.value = BrowseUiState.Success(
                        movies = allLoadedMovies.toList(),
                        hasMore = hasMoreContent,
                        isLoadingMore = false,
                        totalCount = allLoadedMovies.size
                    )
                    isLoadingMore = false
                },
                onFailure = { error ->
                    if (currentPage == 1) {
                        _uiState.value = BrowseUiState.Error(error.message ?: "Failed to load movies")
                    }
                    isLoadingMore = false
                }
            )
        } catch (e: Exception) {
            _uiState.value = BrowseUiState.Error(e.message ?: "Failed to load movies")
            isLoadingMore = false
        }
    }

    fun loadMoreMovies() {
        if (isLoadingMore || !hasMoreContent) return

        viewModelScope.launch {
            isLoadingMore = true
            val currentState = _uiState.value
            if (currentState is BrowseUiState.Success) {
                _uiState.value = currentState.copy(isLoadingMore = true)
            }
            currentPage++
            loadMoviesPage()
        }
    }

    override fun onCleared() {
        super.onCleared()
        allLoadedMovies.clear()
    }
}

sealed class BrowseUiState {
    object Loading : BrowseUiState()
    data class Error(val message: String) : BrowseUiState()
    data class Success(
        val movies: List<TmdbMedia>,
        val hasMore: Boolean = false,
        val isLoadingMore: Boolean = false,
        val totalCount: Int = 0
    ) : BrowseUiState()
}
