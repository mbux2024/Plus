package com.homeflix.tv.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.homeflix.tv.domain.model.*
import com.homeflix.tv.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "homeflix_settings"
)

/**
 * DataStore-backed implementation of SettingsRepository.
 * All settings are persisted locally on device.
 * API keys are stored in plain DataStore (on-device only, no cloud sync).
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val dataStore = context.settingsDataStore

    // ─── Preference Keys ─────────────────────────────────────────────────

    private object Keys {
        // Playback
        val PLAYER_ENGINE = stringPreferencesKey("player_engine")
        val HARDWARE_DECODING = booleanPreferencesKey("hardware_decoding")
        val PREFERRED_QUALITY = stringPreferencesKey("preferred_quality")
        val PREFERRED_AUDIO_LANG = stringPreferencesKey("preferred_audio_language")
        val PREFERRED_SUBTITLE_LANG = stringPreferencesKey("preferred_subtitle_language")
        val SUBTITLE_SIZE = stringPreferencesKey("subtitle_size")
        val AUTOPLAY_NEXT = booleanPreferencesKey("autoplay_next_episode")
        val SKIP_INTRO = booleanPreferencesKey("skip_intro_enabled")

        // Content Discovery
        val TMDB_API_KEY = stringPreferencesKey("tmdb_api_key")
        val TMDB_ENRICH_ARTWORK = booleanPreferencesKey("tmdb_enrich_artwork")
        val TMDB_ENRICH_BASIC = booleanPreferencesKey("tmdb_enrich_basic_info")
        val TMDB_ENRICH_DETAILS = booleanPreferencesKey("tmdb_enrich_details")
        val TMDB_ENRICH_CAST = booleanPreferencesKey("tmdb_enrich_cast")
        val TMDB_ENRICH_TRAILERS = booleanPreferencesKey("tmdb_enrich_trailers")
        val TMDB_ENRICH_MORE_LIKE_THIS = booleanPreferencesKey("tmdb_enrich_more_like_this")
        val TMDB_ENRICH_COLLECTIONS = booleanPreferencesKey("tmdb_enrich_collections")

        // Integration
        val TORBOX_API_KEY = stringPreferencesKey("torbox_api_key")
        val REAL_DEBRID_API_KEY = stringPreferencesKey("real_debrid_api_key")
        val MDBLIST_API_KEY = stringPreferencesKey("mdblist_api_key")
        val OMDB_API_KEY = stringPreferencesKey("omdb_api_key")
        val SHOW_IMDB_RATING = booleanPreferencesKey("show_imdb_rating")
        val SHOW_RT_RATING = booleanPreferencesKey("show_rotten_tomatoes")
        val SHOW_AUDIENCE_SCORE = booleanPreferencesKey("show_audience_score")
        val SHOW_METACRITIC = booleanPreferencesKey("show_metacritic")
        val SHOW_TMDB_RATING = booleanPreferencesKey("show_tmdb_rating")
        val SHOW_TRAKT_RATING = booleanPreferencesKey("show_trakt_rating")
        val SHOW_LETTERBOXD_RATING = booleanPreferencesKey("show_letterboxd_rating")

        // Add-ons
        val STREAM_SOURCE_MODE = stringPreferencesKey("stream_source_mode")
        val CUSTOM_ADDON_URL = stringPreferencesKey("custom_addon_url")
        val SUBTITLE_ADDON_URL = stringPreferencesKey("subtitle_addon_url")
    }

    // ─── Flow ────────────────────────────────────────────────────────────

    override val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        prefsToSettings(prefs)
    }

    override suspend fun getSettings(): AppSettings {
        return settings.first()
    }

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val current = getSettings()
        val updated = transform(current)
        dataStore.edit { prefs ->
            settingsToPrefs(updated, prefs)
        }
    }

    // ─── Convenience Methods ─────────────────────────────────────────────

    override suspend fun setTmdbApiKey(key: String) {
        dataStore.edit { it[Keys.TMDB_API_KEY] = key }
    }

    override suspend fun setTorBoxApiKey(key: String) {
        dataStore.edit { it[Keys.TORBOX_API_KEY] = key }
    }

    override suspend fun setRealDebridApiKey(key: String) {
        dataStore.edit { it[Keys.REAL_DEBRID_API_KEY] = key }
    }

    override suspend fun setMdbListApiKey(key: String) {
        dataStore.edit { it[Keys.MDBLIST_API_KEY] = key }
    }

    override suspend fun setOmdbApiKey(key: String) {
        dataStore.edit { it[Keys.OMDB_API_KEY] = key }
    }

    override suspend fun clearTorBoxApiKey() {
        dataStore.edit { it.remove(Keys.TORBOX_API_KEY) }
    }

    override suspend fun clearRealDebridApiKey() {
        dataStore.edit { it.remove(Keys.REAL_DEBRID_API_KEY) }
    }

    override suspend fun hasDebridService(): Boolean {
        val settings = getSettings()
        return settings.torboxApiKey.isNotBlank() || settings.realDebridApiKey.isNotBlank()
    }

    // ─── Conversion Helpers ──────────────────────────────────────────────

    private fun prefsToSettings(prefs: Preferences): AppSettings {
        return AppSettings(
            // Playback
            playerEngine = prefs[Keys.PLAYER_ENGINE]?.let { enumValueOfSafe<PlayerEngine>(it) }
                ?: PlayerEngine.MPV,
            hardwareDecoding = prefs[Keys.HARDWARE_DECODING] ?: true,
            preferredQuality = prefs[Keys.PREFERRED_QUALITY]?.let { enumValueOfSafe<PreferredQuality>(it) }
                ?: PreferredQuality.BEST_AVAILABLE,
            preferredAudioLanguage = prefs[Keys.PREFERRED_AUDIO_LANG] ?: "en",
            preferredSubtitleLanguage = prefs[Keys.PREFERRED_SUBTITLE_LANG] ?: "en",
            subtitleSize = prefs[Keys.SUBTITLE_SIZE]?.let { enumValueOfSafe<SubtitleSize>(it) }
                ?: SubtitleSize.MEDIUM,
            autoplayNextEpisode = prefs[Keys.AUTOPLAY_NEXT] ?: true,
            skipIntroEnabled = prefs[Keys.SKIP_INTRO] ?: true,

            // Content Discovery
            tmdbApiKey = prefs[Keys.TMDB_API_KEY] ?: "",
            tmdbEnrichArtwork = prefs[Keys.TMDB_ENRICH_ARTWORK] ?: true,
            tmdbEnrichBasicInfo = prefs[Keys.TMDB_ENRICH_BASIC] ?: true,
            tmdbEnrichDetails = prefs[Keys.TMDB_ENRICH_DETAILS] ?: true,
            tmdbEnrichCast = prefs[Keys.TMDB_ENRICH_CAST] ?: true,
            tmdbEnrichTrailers = prefs[Keys.TMDB_ENRICH_TRAILERS] ?: true,
            tmdbEnrichMoreLikeThis = prefs[Keys.TMDB_ENRICH_MORE_LIKE_THIS] ?: true,
            tmdbEnrichCollections = prefs[Keys.TMDB_ENRICH_COLLECTIONS] ?: true,

            // Integration
            torboxApiKey = prefs[Keys.TORBOX_API_KEY] ?: "",
            realDebridApiKey = prefs[Keys.REAL_DEBRID_API_KEY] ?: "",
            mdbListApiKey = prefs[Keys.MDBLIST_API_KEY] ?: "",
            omdbApiKey = prefs[Keys.OMDB_API_KEY] ?: "",
            showImdbRating = prefs[Keys.SHOW_IMDB_RATING] ?: true,
            showRottenTomatoes = prefs[Keys.SHOW_RT_RATING] ?: true,
            showAudienceScore = prefs[Keys.SHOW_AUDIENCE_SCORE] ?: true,
            showMetacritic = prefs[Keys.SHOW_METACRITIC] ?: true,
            showTmdbRating = prefs[Keys.SHOW_TMDB_RATING] ?: true,
            showTraktRating = prefs[Keys.SHOW_TRAKT_RATING] ?: true,
            showLetterboxdRating = prefs[Keys.SHOW_LETTERBOXD_RATING] ?: true,

            // Add-ons
            streamSourceMode = prefs[Keys.STREAM_SOURCE_MODE]?.let { enumValueOfSafe<StreamSourceMode>(it) }
                ?: StreamSourceMode.TORRENTIO_DEBRID,
            customAddonUrl = prefs[Keys.CUSTOM_ADDON_URL] ?: "",
            subtitleAddonUrl = prefs[Keys.SUBTITLE_ADDON_URL] ?: "https://opensubtitles-v3.strem.io"
        )
    }

    private fun settingsToPrefs(settings: AppSettings, prefs: MutablePreferences) {
        // Playback
        prefs[Keys.PLAYER_ENGINE] = settings.playerEngine.name
        prefs[Keys.HARDWARE_DECODING] = settings.hardwareDecoding
        prefs[Keys.PREFERRED_QUALITY] = settings.preferredQuality.name
        prefs[Keys.PREFERRED_AUDIO_LANG] = settings.preferredAudioLanguage
        prefs[Keys.PREFERRED_SUBTITLE_LANG] = settings.preferredSubtitleLanguage
        prefs[Keys.SUBTITLE_SIZE] = settings.subtitleSize.name
        prefs[Keys.AUTOPLAY_NEXT] = settings.autoplayNextEpisode
        prefs[Keys.SKIP_INTRO] = settings.skipIntroEnabled

        // Content Discovery
        prefs[Keys.TMDB_API_KEY] = settings.tmdbApiKey
        prefs[Keys.TMDB_ENRICH_ARTWORK] = settings.tmdbEnrichArtwork
        prefs[Keys.TMDB_ENRICH_BASIC] = settings.tmdbEnrichBasicInfo
        prefs[Keys.TMDB_ENRICH_DETAILS] = settings.tmdbEnrichDetails
        prefs[Keys.TMDB_ENRICH_CAST] = settings.tmdbEnrichCast
        prefs[Keys.TMDB_ENRICH_TRAILERS] = settings.tmdbEnrichTrailers
        prefs[Keys.TMDB_ENRICH_MORE_LIKE_THIS] = settings.tmdbEnrichMoreLikeThis
        prefs[Keys.TMDB_ENRICH_COLLECTIONS] = settings.tmdbEnrichCollections

        // Integration
        prefs[Keys.TORBOX_API_KEY] = settings.torboxApiKey
        prefs[Keys.REAL_DEBRID_API_KEY] = settings.realDebridApiKey
        prefs[Keys.MDBLIST_API_KEY] = settings.mdbListApiKey
        prefs[Keys.OMDB_API_KEY] = settings.omdbApiKey
        prefs[Keys.SHOW_IMDB_RATING] = settings.showImdbRating
        prefs[Keys.SHOW_RT_RATING] = settings.showRottenTomatoes
        prefs[Keys.SHOW_AUDIENCE_SCORE] = settings.showAudienceScore
        prefs[Keys.SHOW_METACRITIC] = settings.showMetacritic
        prefs[Keys.SHOW_TMDB_RATING] = settings.showTmdbRating
        prefs[Keys.SHOW_TRAKT_RATING] = settings.showTraktRating
        prefs[Keys.SHOW_LETTERBOXD_RATING] = settings.showLetterboxdRating

        // Add-ons
        prefs[Keys.STREAM_SOURCE_MODE] = settings.streamSourceMode.name
        prefs[Keys.CUSTOM_ADDON_URL] = settings.customAddonUrl
        prefs[Keys.SUBTITLE_ADDON_URL] = settings.subtitleAddonUrl
    }

    private inline fun <reified T : Enum<T>> enumValueOfSafe(name: String): T? {
        return try {
            enumValueOf<T>(name)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
