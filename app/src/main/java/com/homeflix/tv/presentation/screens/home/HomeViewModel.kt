package com.homeflix.tv.presentation.screens.home

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
class HomeViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val container get() = (getApplication<HomeFlixTVApplication>()).container
    private val tmdb get() = container.tmdbRepository

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadHomeContent() }

    fun loadHomeContent() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val trending = tmdb.trendingMovies()
                val popular = tmdb.topRatedMovies()

                if (trending.isEmpty()) {
                    _uiState.value = HomeUiState.Error("Unable to load content. Check your TMDB API key in Settings.")
                    return@launch
                }

                _uiState.value = HomeUiState.Success(
                    featuredMedia = trending.take(8),
                    trendingMovies = trending,
                    popularMovies = popular,
                    latestMovies = trending,
                    continueWatching = emptyList(),
                    actionMovies = emptyList(),
                    comedyMovies = emptyList(),
                    dramaMovies = emptyList(),
                    sciFiMovies = emptyList(),
                    horrorMovies = emptyList(),
                    romanceMovies = emptyList(),
                    thrillerMovies = emptyList(),
                    currentHeroIndex = 0
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun updateHeroIndex(index: Int) {
        val current = _uiState.value
        if (current is HomeUiState.Success) _uiState.value = current.copy(currentHeroIndex = index)
    }

    fun refreshRecentlyWatched() {}
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Error(val message: String) : HomeUiState()
    data class Success(
        val featuredMedia: List<CatalogItem> = emptyList(),
        val continueWatching: List<com.homeflix.tv.presentation.components.ContinueWatchingItem> = emptyList(),
        val trendingMovies: List<CatalogItem> = emptyList(),
        val popularMovies: List<CatalogItem> = emptyList(),
        val latestMovies: List<CatalogItem> = emptyList(),
        val actionMovies: List<CatalogItem> = emptyList(),
        val comedyMovies: List<CatalogItem> = emptyList(),
        val dramaMovies: List<CatalogItem> = emptyList(),
        val sciFiMovies: List<CatalogItem> = emptyList(),
        val horrorMovies: List<CatalogItem> = emptyList(),
        val romanceMovies: List<CatalogItem> = emptyList(),
        val thrillerMovies: List<CatalogItem> = emptyList(),
        val currentHeroIndex: Int = 0
    ) : HomeUiState()
}
