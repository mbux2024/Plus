package com.homeflix.tv.presentation.screens.mylist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflix.tv.domain.model.*
import com.homeflix.tv.domain.repository.LocalRepository
import com.homeflix.tv.presentation.components.ContinueWatchingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyListViewModel @Inject constructor(
    private val localRepository: LocalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyListUiState>(MyListUiState.Loading)
    val uiState: StateFlow<MyListUiState> = _uiState.asStateFlow()

    init {
        loadMyList()
    }

    fun loadMyList() {
        viewModelScope.launch {
            _uiState.value = MyListUiState.Loading
            try {
                combine(
                    localRepository.getMyList(),
                    localRepository.getContinueWatching()
                ) { myList, continueWatching ->
                    // Convert WatchProgress → ContinueWatchingItem for UI compatibility
                    val continueWatchingItems = continueWatching.map { wp ->
                        val media = TmdbMedia(
                            id = wp.tmdbId,
                            title = wp.title,
                            mediaType = wp.mediaType,
                            posterPath = wp.posterPath,
                            backdropPath = wp.backdropPath ?: wp.stillPath
                        )
                        ContinueWatchingItem(
                            media = media,
                            progress = wp.progressPercent,
                            progressSeconds = wp.progressMs / 1000,
                            lastWatched = formatTimeAgo(wp.lastWatchedAt)
                        )
                    }

                    // Convert MyListItem → TmdbMedia for the .movies field
                    val movies = myList.map { item ->
                        TmdbMedia(
                            id = item.tmdbId,
                            title = item.title,
                            mediaType = item.mediaType,
                            posterPath = item.posterPath,
                            backdropPath = item.backdropPath,
                            voteAverage = item.voteAverage,
                            overview = item.overview
                        )
                    }

                    MyListUiState.Success(
                        myListItems = myList,
                        continueWatching = continueWatchingItems,
                        movies = movies
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                Log.e("MyListViewModel", "Error loading my list", e)
                _uiState.value = MyListUiState.Error(e.message ?: "Failed to load My List")
            }
        }
    }

    fun removeFromMyList(tmdbId: Int, mediaType: TmdbMediaType) {
        viewModelScope.launch {
            localRepository.removeFromMyList(tmdbId, mediaType)
        }
    }
}

sealed class MyListUiState {
    object Loading : MyListUiState()
    data class Success(
        val myListItems: List<MyListItem>,
        val continueWatching: List<ContinueWatchingItem> = emptyList(),
        val movies: List<TmdbMedia> = emptyList()
    ) : MyListUiState()
    data class Error(val message: String) : MyListUiState()
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val hours = diff / (1000 * 60 * 60)
    val days = hours / 24
    return when {
        hours < 1 -> "Just now"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> "${days / 7}w ago"
    }
}
