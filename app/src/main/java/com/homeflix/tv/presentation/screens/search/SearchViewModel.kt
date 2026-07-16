package com.homeflix.tv.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflix.tv.domain.model.TmdbGenre
import com.homeflix.tv.domain.model.TmdbMedia
import com.homeflix.tv.domain.repository.TmdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Initial)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _genres = MutableStateFlow<List<TmdbGenre>>(emptyList())
    val genres: StateFlow<List<TmdbGenre>> = _genres.asStateFlow()

    private val _topSearches = MutableStateFlow<List<TmdbMedia>>(emptyList())
    val topSearches: StateFlow<List<TmdbMedia>> = _topSearches.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Load genres
            tmdbRepository.getMovieGenres().fold(
                onSuccess = { _genres.value = it },
                onFailure = { android.util.Log.e("SearchViewModel", "Failed to load genres", it) }
            )
        }

        viewModelScope.launch {
            // Load top searches (popular movies as suggestions)
            tmdbRepository.getPopularMovies().fold(
                onSuccess = { _topSearches.value = it.take(8) },
                onFailure = { android.util.Log.e("SearchViewModel", "Failed to load top searches", it) }
            )
        }
    }

    fun searchMedia(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = SearchUiState.Initial
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _uiState.value = SearchUiState.Loading

            tmdbRepository.searchMulti(query).fold(
                onSuccess = { results ->
                    _uiState.value = SearchUiState.Success(results)
                },
                onFailure = { error ->
                    _uiState.value = SearchUiState.Error(error.message ?: "Search failed")
                }
            )
        }
    }

    fun searchByGenre(genreId: Int) {
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _uiState.value = SearchUiState.Loading

            tmdbRepository.discoverByGenre(genreId, com.homeflix.tv.domain.model.TmdbMediaType.MOVIE).fold(
                onSuccess = { results ->
                    _uiState.value = SearchUiState.Success(results)
                },
                onFailure = { error ->
                    _uiState.value = SearchUiState.Error(error.message ?: "Genre search failed")
                }
            )
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.value = SearchUiState.Initial
    }
}

sealed class SearchUiState {
    object Initial : SearchUiState()
    object Loading : SearchUiState()
    data class Error(val message: String) : SearchUiState()
    data class Success(val results: List<TmdbMedia>) : SearchUiState()
}
