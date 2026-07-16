package com.homeflix.tv.presentation.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflix.tv.domain.model.*
import com.homeflix.tv.domain.repository.LocalRepository
import com.homeflix.tv.domain.repository.TmdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"

/**
 * HOME SCREEN — TMDB-powered catalog.
 *
 * Rows:
 * - Continue Watching (from local Room DB)
 * - Trending Movies
 * - Trending TV Shows
 * - Top 10 Movies Today (with big rank numerals)
 * - Top 10 TV Today
 * - Airing Today in the U.S.
 * - Top Rated Movies
 * - Top Rated TV Shows
 * - Genre rows (cover-art cards that open all movies+TV in that genre)
 *
 * Services row is handled by a separate ServicesViewModel/screen.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Continue watching from local DB (reactive)
    val continueWatching: StateFlow<List<WatchProgress>> = localRepository.getContinueWatching()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadHomeContent()
    }

    fun loadHomeContent() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            try {
                // Fetch all rows in parallel
                val trendingMoviesDeferred = async { tmdbRepository.getTrendingMovies() }
                val trendingTvDeferred = async { tmdbRepository.getTrendingTv() }
                val top10MoviesDeferred = async { tmdbRepository.getTop10Movies() }
                val top10TvDeferred = async { tmdbRepository.getTop10Tv() }
                val topRatedMoviesDeferred = async { tmdbRepository.getTopRatedMovies() }
                val topRatedTvDeferred = async { tmdbRepository.getTopRatedTv() }
                val airingTodayDeferred = async { tmdbRepository.getAiringTodayTv() }
                val popularMoviesDeferred = async { tmdbRepository.getPopularMovies() }
                val genresDeferred = async { tmdbRepository.getMovieGenres() }

                val trendingMovies = trendingMoviesDeferred.await().getOrDefault(emptyList())
                val trendingTv = trendingTvDeferred.await().getOrDefault(emptyList())
                val top10Movies = top10MoviesDeferred.await().getOrDefault(emptyList())
                val top10Tv = top10TvDeferred.await().getOrDefault(emptyList())
                val topRatedMovies = topRatedMoviesDeferred.await().getOrDefault(emptyList())
                val topRatedTv = topRatedTvDeferred.await().getOrDefault(emptyList())
                val airingToday = airingTodayDeferred.await().getOrDefault(emptyList())
                val popularMovies = popularMoviesDeferred.await().getOrDefault(emptyList())
                val genres = genresDeferred.await().getOrDefault(emptyList())

                // Hero section: top trending movies with backdrop
                val heroItems = trendingMovies
                    .filter { it.backdropPath != null }
                    .take(8)

                if (heroItems.isEmpty() && trendingMovies.isEmpty()) {
                    _uiState.value = HomeUiState.Error(
                        "Unable to load content. Check your TMDB API key in Settings."
                    )
                    return@launch
                }

                _uiState.value = HomeUiState.Success(
                    heroItems = heroItems,
                    trendingMovies = trendingMovies,
                    trendingTv = trendingTv,
                    top10Movies = top10Movies,
                    top10Tv = top10Tv,
                    topRatedMovies = topRatedMovies,
                    topRatedTv = topRatedTv,
                    airingToday = airingToday,
                    popularMovies = popularMovies,
                    genres = genres,
                    currentHeroIndex = 0
                )

                Log.d(TAG, "Home loaded: ${trendingMovies.size} trending, ${topRatedMovies.size} top rated")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading home content", e)
                _uiState.value = HomeUiState.Error(
                    "Error: ${e.message ?: "Unknown error"}. Check your TMDB API key in Settings."
                )
            }
        }
    }

    fun updateHeroIndex(index: Int) {
        val current = _uiState.value
        if (current is HomeUiState.Success) {
            _uiState.value = current.copy(currentHeroIndex = index)
        }
    }

    fun refresh() {
        loadHomeContent()
    }

    /** Backward compat: called by NetflixHomeScreen on lifecycle resume */
    fun refreshRecentlyWatched() {
        // Continue watching updates reactively via Flow — no action needed
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Error(val message: String) : HomeUiState()
    data class Success(
        val heroItems: List<TmdbMedia>,
        val trendingMovies: List<TmdbMedia>,
        val trendingTv: List<TmdbMedia>,
        val top10Movies: List<TmdbMedia>,
        val top10Tv: List<TmdbMedia>,
        val topRatedMovies: List<TmdbMedia>,
        val topRatedTv: List<TmdbMedia>,
        val airingToday: List<TmdbMedia>,
        val popularMovies: List<TmdbMedia>,
        val genres: List<TmdbGenre>,
        val currentHeroIndex: Int = 0,
        // Backward-compatible fields for NetflixHomeScreen
        val featuredMedia: List<TmdbMedia> = heroItems,
        val continueWatching: List<com.homeflix.tv.presentation.components.ContinueWatchingItem> = emptyList(),
        val latestMovies: List<TmdbMedia> = popularMovies,
        val actionMovies: List<TmdbMedia> = emptyList(),
        val comedyMovies: List<TmdbMedia> = emptyList(),
        val dramaMovies: List<TmdbMedia> = emptyList(),
        val sciFiMovies: List<TmdbMedia> = emptyList(),
        val horrorMovies: List<TmdbMedia> = emptyList(),
        val romanceMovies: List<TmdbMedia> = emptyList(),
        val thrillerMovies: List<TmdbMedia> = emptyList()
    ) : HomeUiState()
}
