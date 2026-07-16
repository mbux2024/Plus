package com.homeflix.tv.presentation.screens.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homeflix.tv.HomeFlixTVApplication
import com.homeflix.tv.data.model.CatalogItem
import com.homeflix.tv.data.tmdb.TmdbGenre
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val tmdb get() = (getApplication<HomeFlixTVApplication>()).container.tmdbRepository
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Initial)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private val _genres = MutableStateFlow<List<TmdbGenre>>(emptyList())
    val genres: StateFlow<List<TmdbGenre>> = _genres.asStateFlow()
    private val _topSearches = MutableStateFlow<List<CatalogItem>>(emptyList())
    val topSearches: StateFlow<List<CatalogItem>> = _topSearches.asStateFlow()
    private var searchJob: Job? = null
    init { viewModelScope.launch { _topSearches.value = tmdb.trendingMovies().take(8) } }
    fun searchMedia(query: String) { searchJob?.cancel(); if (query.isBlank()) { _uiState.value = SearchUiState.Initial; return }
        searchJob = viewModelScope.launch { delay(300); _uiState.value = SearchUiState.Loading
            try { _uiState.value = SearchUiState.Success(tmdb.search(query)) } catch (e: Exception) { _uiState.value = SearchUiState.Error(e.message ?: "Error") }
    }}
    fun searchByGenre(genreName: String) {}
    fun clearSearch() { searchJob?.cancel(); _uiState.value = SearchUiState.Initial }
}
sealed class SearchUiState {
    object Initial : SearchUiState(); object Loading : SearchUiState()
    data class Error(val message: String) : SearchUiState()
    data class Success(val results: List<CatalogItem>) : SearchUiState()
}
