package com.homeflix.tv.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflix.tv.data.remote.debrid.DebridResolver
import com.homeflix.tv.data.remote.stremio.AddonManager
import com.homeflix.tv.data.remote.stremio.AddonWebServer
import com.homeflix.tv.domain.model.*
import com.homeflix.tv.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val debridResolver: DebridResolver,
    private val addonManager: AddonManager,
    private val addonWebServer: AddonWebServer
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val installedAddons: StateFlow<List<StremioAddon>> = addonManager.getInstalledAddons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _torboxStatus = MutableStateFlow<DebridConnectionStatus>(DebridConnectionStatus.NotConnected)
    val torboxStatus: StateFlow<DebridConnectionStatus> = _torboxStatus.asStateFlow()

    private val _rdStatus = MutableStateFlow<DebridConnectionStatus>(DebridConnectionStatus.NotConnected)
    val rdStatus: StateFlow<DebridConnectionStatus> = _rdStatus.asStateFlow()

    private val _addonWebServerUrl = MutableStateFlow<String?>(null)
    val addonWebServerUrl: StateFlow<String?> = _addonWebServerUrl.asStateFlow()

    init {
        // Check connection status on init
        viewModelScope.launch {
            settings.collect { s ->
                if (s.torboxApiKey.isNotBlank()) {
                    _torboxStatus.value = DebridConnectionStatus.Connected
                }
                if (s.realDebridApiKey.isNotBlank()) {
                    _rdStatus.value = DebridConnectionStatus.Connected
                }
            }
        }
    }

    // ─── Playback Settings ───────────────────────────────────────────────

    fun setPlayerEngine(engine: PlayerEngine) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(playerEngine = engine) }
        }
    }

    fun setHardwareDecoding(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(hardwareDecoding = enabled) }
        }
    }

    fun setPreferredQuality(quality: PreferredQuality) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(preferredQuality = quality) }
        }
    }

    fun setPreferredAudioLanguage(lang: String) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(preferredAudioLanguage = lang) }
        }
    }

    fun setPreferredSubtitleLanguage(lang: String) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(preferredSubtitleLanguage = lang) }
        }
    }

    fun setSubtitleSize(size: SubtitleSize) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(subtitleSize = size) }
        }
    }

    fun setAutoplayNext(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(autoplayNextEpisode = enabled) }
        }
    }

    fun setSkipIntro(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(skipIntroEnabled = enabled) }
        }
    }

    // ─── Content Discovery Settings ──────────────────────────────────────

    fun setTmdbApiKey(key: String) {
        viewModelScope.launch { settingsRepository.setTmdbApiKey(key) }
    }

    fun setTmdbEnrichment(field: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { s ->
                when (field) {
                    "artwork" -> s.copy(tmdbEnrichArtwork = enabled)
                    "basic" -> s.copy(tmdbEnrichBasicInfo = enabled)
                    "details" -> s.copy(tmdbEnrichDetails = enabled)
                    "cast" -> s.copy(tmdbEnrichCast = enabled)
                    "trailers" -> s.copy(tmdbEnrichTrailers = enabled)
                    "more_like_this" -> s.copy(tmdbEnrichMoreLikeThis = enabled)
                    "collections" -> s.copy(tmdbEnrichCollections = enabled)
                    else -> s
                }
            }
        }
    }

    // ─── Integration Settings ────────────────────────────────────────────

    fun connectTorBox(apiKey: String) {
        viewModelScope.launch {
            _torboxStatus.value = DebridConnectionStatus.Connecting
            val result = debridResolver.verifyTorBoxKey(apiKey)
            result.fold(
                onSuccess = {
                    settingsRepository.setTorBoxApiKey(apiKey)
                    _torboxStatus.value = DebridConnectionStatus.Connected
                },
                onFailure = {
                    _torboxStatus.value = DebridConnectionStatus.Error(it.message ?: "Connection failed")
                }
            )
        }
    }

    fun disconnectTorBox() {
        viewModelScope.launch {
            settingsRepository.clearTorBoxApiKey()
            _torboxStatus.value = DebridConnectionStatus.NotConnected
        }
    }

    fun connectRealDebrid(apiKey: String) {
        viewModelScope.launch {
            _rdStatus.value = DebridConnectionStatus.Connecting
            val result = debridResolver.verifyRealDebridKey(apiKey)
            result.fold(
                onSuccess = {
                    settingsRepository.setRealDebridApiKey(apiKey)
                    _rdStatus.value = DebridConnectionStatus.Connected
                },
                onFailure = {
                    _rdStatus.value = DebridConnectionStatus.Error(it.message ?: "Connection failed")
                }
            )
        }
    }

    fun disconnectRealDebrid() {
        viewModelScope.launch {
            settingsRepository.clearRealDebridApiKey()
            _rdStatus.value = DebridConnectionStatus.NotConnected
        }
    }

    fun setMdbListApiKey(key: String) {
        viewModelScope.launch { settingsRepository.setMdbListApiKey(key) }
    }

    fun setOmdbApiKey(key: String) {
        viewModelScope.launch { settingsRepository.setOmdbApiKey(key) }
    }

    fun setRatingToggle(source: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { s ->
                when (source) {
                    "imdb" -> s.copy(showImdbRating = enabled)
                    "rt" -> s.copy(showRottenTomatoes = enabled)
                    "audience" -> s.copy(showAudienceScore = enabled)
                    "metacritic" -> s.copy(showMetacritic = enabled)
                    "tmdb" -> s.copy(showTmdbRating = enabled)
                    "trakt" -> s.copy(showTraktRating = enabled)
                    "letterboxd" -> s.copy(showLetterboxdRating = enabled)
                    else -> s
                }
            }
        }
    }

    // ─── Add-ons Settings ────────────────────────────────────────────────

    fun setStreamSourceMode(mode: StreamSourceMode) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(streamSourceMode = mode) }
        }
    }

    fun setCustomAddonUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(customAddonUrl = url) }
        }
    }

    fun installAddon(manifestUrl: String) {
        viewModelScope.launch {
            addonManager.installAddon(manifestUrl)
        }
    }

    fun removeAddon(addonId: String) {
        viewModelScope.launch {
            addonManager.removeAddon(addonId)
        }
    }

    fun toggleAddon(addonId: String, enabled: Boolean) {
        viewModelScope.launch {
            addonManager.toggleAddon(addonId, enabled)
        }
    }

    fun reorderAddon(addonId: String, newPriority: Int) {
        viewModelScope.launch {
            addonManager.reorderAddon(addonId, newPriority)
        }
    }

    fun refreshAddon(addonId: String) {
        viewModelScope.launch {
            addonManager.refreshAddon(addonId)
        }
    }

    // ─── QR Addon Web Server ─────────────────────────────────────────────

    fun startAddonWebServer(context: android.content.Context) {
        val url = addonWebServer.start(context)
        _addonWebServerUrl.value = url
    }

    fun stopAddonWebServer() {
        addonWebServer.stop()
        _addonWebServerUrl.value = null
    }

    override fun onCleared() {
        super.onCleared()
        addonWebServer.stop()
    }
}

sealed class DebridConnectionStatus {
    object NotConnected : DebridConnectionStatus()
    object Connecting : DebridConnectionStatus()
    object Connected : DebridConnectionStatus()
    data class Error(val message: String) : DebridConnectionStatus()
}

/**
 * Settings categories for the left-rail navigation.
 */
enum class SettingsCategory(val label: String) {
    PLAYBACK("Playback"),
    CONTENT_DISCOVERY("Content Discovery"),
    INTEGRATION("Integration"),
    ADDONS("Add-ons"),
    ABOUT("About");
}
