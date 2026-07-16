package com.homeflix.tv.presentation.screens.mylist

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
class MyListViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val myList get() = (getApplication<HomeFlixTVApplication>()).container.myListRepository
    private val _uiState = MutableStateFlow<MyListUiState>(MyListUiState.Loading)
    val uiState: StateFlow<MyListUiState> = _uiState.asStateFlow()
    init { loadMyList() }
    fun loadMyList() { viewModelScope.launch { _uiState.value = MyListUiState.Success(movies = emptyList(), continueWatching = emptyList()) } }
}
sealed class MyListUiState {
    object Loading : MyListUiState()
    data class Success(val movies: List<CatalogItem> = emptyList(), val continueWatching: List<com.homeflix.tv.presentation.components.ContinueWatchingItem> = emptyList()) : MyListUiState()
    data class Error(val message: String) : MyListUiState()
}
