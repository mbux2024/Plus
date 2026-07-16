package com.homeflix.tv.presentation.screens.details

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflix.tv.data.resolver.TrailerResolver
import com.homeflix.tv.domain.model.*
import com.homeflix.tv.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "DetailsViewModel"

/**
 * Detail page ViewModel — ARVIO-style.
 * Loads TMDB details, credits, videos, similar, external IDs.
 * Resolves streams on Play, provides source picker, trailer playback.
 */
@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tmdbRepository: TmdbRepository,
    private val streamRepository: StreamRepository,
    private val localRepository: LocalRepository,
    private val settingsRepository: SettingsRepository,
    private val trailerResolver: TrailerResolver
) : ViewModel() {

    private val tmdbId: Int = savedStateHandle.get<Int>("tmdbId") ?: 0
    private val mediaTypeStr: String = savedStateHandle.get<String>("mediaType") ?: "MOVIE"
    val mediaType: TmdbMediaType = if (mediaTypeStr == "TV") TmdbMediaType.TV else TmdbMediaType.MOVIE

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private val _streamState = MutableStateFlow<StreamState>(StreamState.Idle)
    val streamState: StateFlow<StreamState> = _streamState.asStateFlow()

    private val _isInMyList = MutableStateFlow(false)
    val isInMyList: StateFlow<Boolean> = _isInMyList.asStateFlow()

    private val _episodes = MutableStateFlow<List<TmdbEpisode>>(emptyList())
    val episodes: StateFlow<List<TmdbEpisode>> = _episodes.asStateFlow()

    // Cached external IDs for stream resolution
    private var externalIds: ExternalIds? = null

    init {
        loadDetails()
        checkMyList()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            try {
                val detailResult = when (mediaType) {
                    TmdbMediaType.MOVIE -> tmdbRepository.getMovieDetails(tmdbId)
                    TmdbMediaType.TV -> tmdbRepository.getTvDetails(tmdbId)
                }

                detailResult.fold(
                    onSuccess = { detail ->
                        _uiState.value = DetailsUiState.Success(detail)
                        // Pre-fetch external IDs for stream resolution
                        fetchExternalIds()
                    },
                    onFailure = { error ->
                        _uiState.value = DetailsUiState.Error(error.message ?: "Failed to load details")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading details", e)
                _uiState.value = DetailsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun fetchExternalIds() {
        viewModelScope.launch {
            tmdbRepository.getExternalIds(tmdbId, mediaType).fold(
                onSuccess = { ids -> externalIds = ids },
                onFailure = { Log.w(TAG, "Failed to fetch external IDs: ${it.message}") }
            )
        }
    }

    // ─── Season/Episode Loading ──────────────────────────────────────────

    fun loadSeason(seasonNumber: Int) {
        viewModelScope.launch {
            tmdbRepository.getSeasonDetails(tmdbId, seasonNumber).fold(
                onSuccess = { episodeList -> _episodes.value = episodeList },
                onFailure = { Log.e(TAG, "Error loading season: ${it.message}") }
            )
        }
    }

    // ─── Stream Resolution ───────────────────────────────────────────────

    /**
     * Auto-play: resolve the best stream and return URL.
     */
    fun playBest(season: Int? = null, episode: Int? = null) {
        viewModelScope.launch {
            _streamState.value = StreamState.Resolving
            val imdbId = externalIds?.imdbId
            if (imdbId == null) {
                _streamState.value = StreamState.Error("No IMDb ID found for this title")
                return@launch
            }

            val settings = settingsRepository.getSettings()
            val result = streamRepository.getBestStream(
                imdbId = imdbId,
                mediaType = mediaType,
                season = season,
                episode = episode,
                preferredQuality = settings.preferredQuality
            )

            result.fold(
                onSuccess = { source ->
                    if (source.url != null) {
                        _streamState.value = StreamState.Ready(source.url, source)
                    } else if (source.infoHash != null) {
                        // Need debrid resolution
                        val resolved = streamRepository.resolveDebridStream(source.infoHash, source.fileIndex)
                        resolved.fold(
                            onSuccess = { url -> _streamState.value = StreamState.Ready(url, source) },
                            onFailure = { _streamState.value = StreamState.Error(it.message ?: "Debrid resolve failed") }
                        )
                    } else {
                        _streamState.value = StreamState.Error("No playable URL")
                    }
                },
                onFailure = { _streamState.value = StreamState.Error(it.message ?: "No streams found") }
            )
        }
    }

    /**
     * Load all available sources for the picker panel.
     */
    fun loadSources(season: Int? = null, episode: Int? = null) {
        viewModelScope.launch {
            _streamState.value = StreamState.Resolving
            val imdbId = externalIds?.imdbId
            if (imdbId == null) {
                _streamState.value = StreamState.Error("No IMDb ID")
                return@launch
            }

            val result = if (mediaType == TmdbMediaType.MOVIE) {
                streamRepository.resolveMovieStreams(imdbId)
            } else {
                if (season != null && episode != null) {
                    streamRepository.resolveEpisodeStreams(imdbId, season, episode)
                } else {
                    Result.failure(Exception("Season and episode required"))
                }
            }

            result.fold(
                onSuccess = { sources -> _streamState.value = StreamState.SourcesLoaded(sources) },
                onFailure = { _streamState.value = StreamState.Error(it.message ?: "Failed to load sources") }
            )
        }
    }

    /**
     * Play a specific source from the picker.
     */
    fun playSource(source: StreamSource) {
        viewModelScope.launch {
            _streamState.value = StreamState.Resolving
            if (source.url != null) {
                _streamState.value = StreamState.Ready(source.url, source)
            } else if (source.infoHash != null) {
                val resolved = streamRepository.resolveDebridStream(source.infoHash, source.fileIndex)
                resolved.fold(
                    onSuccess = { url -> _streamState.value = StreamState.Ready(url, source) },
                    onFailure = { _streamState.value = StreamState.Error(it.message ?: "Resolve failed") }
                )
            }
        }
    }

    // ─── Trailer ─────────────────────────────────────────────────────────

    fun playTrailer(youtubeId: String) {
        viewModelScope.launch {
            _streamState.value = StreamState.Resolving
            trailerResolver.resolve(youtubeId).fold(
                onSuccess = { trailer ->
                    _streamState.value = StreamState.TrailerReady(trailer.streamUrl, trailer.title)
                },
                onFailure = {
                    _streamState.value = StreamState.Error("Trailer unavailable: ${it.message}")
                }
            )
        }
    }

    // ─── My List ─────────────────────────────────────────────────────────

    private fun checkMyList() {
        viewModelScope.launch {
            _isInMyList.value = localRepository.isInMyList(tmdbId, mediaType)
        }
    }

    fun toggleMyList() {
        viewModelScope.launch {
            val detail = (_uiState.value as? DetailsUiState.Success)?.detail ?: return@launch
            val media = detail.media

            if (_isInMyList.value) {
                localRepository.removeFromMyList(tmdbId, mediaType)
                _isInMyList.value = false
            } else {
                localRepository.addToMyList(
                    MyListItem(
                        tmdbId = tmdbId,
                        mediaType = mediaType,
                        title = media.title,
                        posterPath = media.posterPath,
                        backdropPath = media.backdropPath,
                        voteAverage = media.voteAverage,
                        overview = media.overview
                    )
                )
                _isInMyList.value = true
            }
        }
    }

    fun resetStreamState() {
        _streamState.value = StreamState.Idle
    }
}

sealed class DetailsUiState {
    object Loading : DetailsUiState()
    data class Error(val message: String) : DetailsUiState()
    data class Success(val detail: TmdbMediaDetail) : DetailsUiState()
}

sealed class StreamState {
    object Idle : StreamState()
    object Resolving : StreamState()
    data class Ready(val url: String, val source: StreamSource) : StreamState()
    data class TrailerReady(val url: String, val title: String) : StreamState()
    data class SourcesLoaded(val sources: List<StreamSource>) : StreamState()
    data class Error(val message: String) : StreamState()
}
