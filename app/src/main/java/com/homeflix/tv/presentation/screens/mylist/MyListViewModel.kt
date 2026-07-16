package com.homeflix.tv.presentation.screens.mylist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflix.tv.domain.model.MyListItem
import com.homeflix.tv.domain.model.WatchProgress
import com.homeflix.tv.domain.repository.LocalRepository
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
                // Combine My List and Continue Watching from local DB
                combine(
                    localRepository.getMyList(),
                    localRepository.getContinueWatching()
                ) { myList, continueWatching ->
                    MyListUiState.Success(
                        myListItems = myList,
                        continueWatching = continueWatching
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

    fun removeFromMyList(tmdbId: Int, mediaType: com.homeflix.tv.domain.model.TmdbMediaType) {
        viewModelScope.launch {
            localRepository.removeFromMyList(tmdbId, mediaType)
        }
    }
}

sealed class MyListUiState {
    object Loading : MyListUiState()
    data class Success(
        val myListItems: List<MyListItem>,
        val continueWatching: List<WatchProgress> = emptyList()
    ) : MyListUiState()
    data class Error(val message: String) : MyListUiState()
}
