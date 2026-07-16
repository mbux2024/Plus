package com.homeflix.tv.domain.repository

import com.homeflix.tv.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository for app settings persistence via DataStore.
 */
interface SettingsRepository {
    
    /**
     * Observe settings changes as a Flow.
     */
    val settings: Flow<AppSettings>

    /**
     * Get current settings snapshot.
     */
    suspend fun getSettings(): AppSettings

    /**
     * Update settings. Only provided fields are changed.
     */
    suspend fun updateSettings(transform: (AppSettings) -> AppSettings)

    // ─── Convenience methods for common operations ───────────────────────

    suspend fun setTmdbApiKey(key: String)
    suspend fun setTorBoxApiKey(key: String)
    suspend fun setRealDebridApiKey(key: String)
    suspend fun setMdbListApiKey(key: String)
    suspend fun setOmdbApiKey(key: String)

    suspend fun clearTorBoxApiKey()
    suspend fun clearRealDebridApiKey()

    /**
     * Check if at least one debrid service is configured.
     */
    suspend fun hasDebridService(): Boolean
}
