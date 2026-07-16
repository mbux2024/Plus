package com.homeflix.tv.data.remote.stremio

import android.util.Log
import com.homeflix.tv.domain.model.*
import com.homeflix.tv.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AddonManager"

/**
 * Manages installed Stremio addons.
 * Handles installation from manifest URL, removal, reordering, and provides
 * the list of active addons for stream resolution.
 *
 * Built-in addons (Torrentio no-debrid, Comet) are pre-installed and cannot be removed.
 */
@Singleton
class AddonManager @Inject constructor(
    private val addonClient: StremioAddonClient,
    private val localRepository: LocalRepository
) {

    companion object {
        // ─── Built-in addon definitions ──────────────────────────────────

        val TORRENTIO_NO_DEBRID = StremioAddon(
            id = "com.stremio.torrentio.addon",
            name = "Torrentio",
            version = "0.0.14",
            description = "Torrent streams (no debrid baked in — resolved by app)",
            manifestUrl = "https://torrentio.strem.fun/manifest.json",
            baseUrl = "https://torrentio.strem.fun",
            types = listOf("movie", "series"),
            resources = listOf("stream"),
            addonType = StremioAddonType.STREAM,
            priority = 0,
            isEnabled = true,
            isBuiltIn = true,
            requiresDebrid = true
        )

        val COMET_DEFAULT = StremioAddon(
            id = "com.stremio.comet.addon",
            name = "Comet",
            version = "1.0.0",
            description = "Comet addon for anime and niche content",
            manifestUrl = "https://comet.elfhosted.com/manifest.json",
            baseUrl = "https://comet.elfhosted.com",
            types = listOf("movie", "series"),
            resources = listOf("stream"),
            addonType = StremioAddonType.STREAM,
            priority = 1,
            isEnabled = true,
            isBuiltIn = true,
            requiresDebrid = true
        )

        val OPENSUBTITLES_DEFAULT = StremioAddon(
            id = "com.stremio.opensubtitlesv3.addon",
            name = "OpenSubtitles v3 Pro",
            version = "1.0.0",
            description = "Subtitles from OpenSubtitles",
            manifestUrl = "https://opensubtitles-v3.strem.io/manifest.json",
            baseUrl = "https://opensubtitles-v3.strem.io",
            types = listOf("movie", "series"),
            resources = listOf("subtitles"),
            addonType = StremioAddonType.SUBTITLES,
            priority = 100,
            isEnabled = true,
            isBuiltIn = true,
            requiresDebrid = false
        )

        val DEFAULT_ADDONS = listOf(TORRENTIO_NO_DEBRID, COMET_DEFAULT, OPENSUBTITLES_DEFAULT)
    }

    // ─── Addon Access ────────────────────────────────────────────────────

    /**
     * Get all installed addons as a Flow (updates when addons change).
     */
    fun getInstalledAddons(): Flow<List<StremioAddon>> = localRepository.getInstalledAddons()

    /**
     * Get currently active stream addons sorted by priority.
     */
    suspend fun getActiveStreamAddons(): List<StremioAddon> {
        return localRepository.getInstalledAddons().first()
            .filter { it.isEnabled && it.addonType == StremioAddonType.STREAM }
            .sortedBy { it.priority }
    }

    /**
     * Get the active subtitle addon (first enabled subtitle addon).
     */
    suspend fun getActiveSubtitleAddon(): StremioAddon? {
        return localRepository.getInstalledAddons().first()
            .firstOrNull { it.isEnabled && it.addonType == StremioAddonType.SUBTITLES }
    }

    // ─── Install / Remove ────────────────────────────────────────────────

    /**
     * Install a new addon from its manifest URL.
     * Fetches the manifest, validates it, and stores the addon.
     */
    suspend fun installAddon(manifestUrl: String): Result<StremioAddon> {
        return try {
            Log.d(TAG, "Installing addon from: $manifestUrl")

            // Fetch and parse manifest
            val manifest = addonClient.fetchManifest(manifestUrl).getOrElse {
                return Result.failure(Exception("Failed to fetch manifest: ${it.message}"))
            }

            // Validate — must have at least streams or subtitles
            if (!manifest.hasStreams && !manifest.hasSubtitles) {
                return Result.failure(Exception("Addon has no stream or subtitle resources"))
            }

            // Determine addon type
            val addonType = when {
                manifest.hasStreams -> StremioAddonType.STREAM
                manifest.hasSubtitles -> StremioAddonType.SUBTITLES
                else -> StremioAddonType.CATALOG
            }

            // Calculate priority (append to end)
            val existingAddons = localRepository.getInstalledAddons().first()
            val maxPriority = existingAddons.maxOfOrNull { it.priority } ?: -1

            val baseUrl = addonClient.extractBaseUrl(manifestUrl)

            val addon = StremioAddon(
                id = manifest.id,
                name = manifest.name,
                version = manifest.version,
                description = manifest.description,
                manifestUrl = addonClient.extractBaseUrl(manifestUrl) + "/manifest.json",
                baseUrl = baseUrl,
                types = manifest.types,
                resources = manifest.resourceNames,
                addonType = addonType,
                priority = maxPriority + 1,
                isEnabled = true,
                isBuiltIn = false,
                requiresDebrid = false // User-installed addons may or may not need debrid
            )

            // Check if already installed
            val existing = existingAddons.find { it.id == addon.id }
            if (existing != null) {
                // Update existing addon (refresh version/name from manifest)
                localRepository.removeAddon(existing.id)
            }

            localRepository.installAddon(addon)
            Log.d(TAG, "Addon installed: ${addon.name} (${addon.id})")
            Result.success(addon)
        } catch (e: Exception) {
            Log.e(TAG, "Error installing addon: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Remove an installed addon (built-in addons cannot be removed, only disabled).
     */
    suspend fun removeAddon(addonId: String): Result<Unit> {
        return try {
            val addons = localRepository.getInstalledAddons().first()
            val addon = addons.find { it.id == addonId }
                ?: return Result.failure(Exception("Addon not found"))

            if (addon.isBuiltIn) {
                // Can't remove built-in — just disable
                localRepository.toggleAddon(addonId, false)
                Log.d(TAG, "Built-in addon disabled: $addonId")
            } else {
                localRepository.removeAddon(addonId)
                Log.d(TAG, "Addon removed: $addonId")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing addon: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Reorder an addon (change priority).
     * Lower priority number = queried first.
     */
    suspend fun reorderAddon(addonId: String, newPriority: Int): Result<Unit> {
        return try {
            localRepository.updateAddonPriority(addonId, newPriority)
            Log.d(TAG, "Addon reordered: $addonId -> priority $newPriority")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error reordering addon: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Toggle an addon enabled/disabled.
     */
    suspend fun toggleAddon(addonId: String, enabled: Boolean): Result<Unit> {
        return try {
            localRepository.toggleAddon(addonId, enabled)
            Log.d(TAG, "Addon toggled: $addonId -> enabled=$enabled")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling addon: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Refresh an addon's manifest (update name/version without reinstalling).
     */
    suspend fun refreshAddon(addonId: String): Result<StremioAddon> {
        val addons = localRepository.getInstalledAddons().first()
        val existing = addons.find { it.id == addonId }
            ?: return Result.failure(Exception("Addon not found"))

        return installAddon(existing.manifestUrl)
    }

    /**
     * Initialize default addons on first launch.
     * Only installs if no addons exist in the database.
     */
    suspend fun initializeDefaultAddons() {
        val existing = localRepository.getInstalledAddons().first()
        if (existing.isEmpty()) {
            Log.d(TAG, "No addons found — installing defaults")
            DEFAULT_ADDONS.forEach { addon ->
                localRepository.installAddon(addon)
            }
        }
    }
}
