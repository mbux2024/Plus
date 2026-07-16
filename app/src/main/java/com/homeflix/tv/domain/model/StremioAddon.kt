package com.homeflix.tv.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A Stremio addon installed in the app.
 * Can be a stream addon (Torrentio, Comet) or a subtitle addon (OpenSubtitles).
 */
@Parcelize
data class StremioAddon(
    val id: String,
    val name: String,
    val version: String = "",
    val description: String = "",
    val manifestUrl: String,        // Full URL to manifest.json
    val baseUrl: String,            // Base URL for stream/subtitle endpoints
    val types: List<String> = emptyList(),       // ["movie", "series"]
    val resources: List<String> = emptyList(),   // ["stream", "subtitles", "catalog"]
    val addonType: StremioAddonType = StremioAddonType.STREAM,
    val priority: Int = 0,          // Lower = higher priority (user can reorder)
    val isEnabled: Boolean = true,
    val isBuiltIn: Boolean = false, // Torrentio (no-debrid) and Comet are built-in
    val requiresDebrid: Boolean = false // Whether this addon needs debrid key to work
) : Parcelable

enum class StremioAddonType {
    STREAM,     // Provides stream sources (Torrentio, Comet, etc.)
    SUBTITLES,  // Provides subtitles (OpenSubtitles v3)
    CATALOG     // Provides catalog (not used - we use TMDB)
}

/**
 * Raw stream response from a Stremio addon's stream endpoint.
 * /stream/{type}/{id}.json
 */
@Parcelize
data class StremioStreamResponse(
    val streams: List<StremioStream> = emptyList()
) : Parcelable

/**
 * A single stream entry from a Stremio addon.
 * May contain a direct URL (debrid-backed addon) or infoHash (raw torrent).
 */
@Parcelize
data class StremioStream(
    val url: String? = null,
    val infoHash: String? = null,
    val fileIdx: Int? = null,
    val title: String? = null,      // Contains release name + quality info
    val name: String? = null,       // Addon display name / quality label
    val behaviorHints: StremioStreamBehaviorHints? = null
) : Parcelable

@Parcelize
data class StremioStreamBehaviorHints(
    val bingeGroup: String? = null,
    val filename: String? = null,
    val videoSize: Long? = null
) : Parcelable

/**
 * Subtitle response from a Stremio subtitles addon.
 * /subtitles/{type}/{id}.json
 */
@Parcelize
data class StremioSubtitleResponse(
    val subtitles: List<StremioSubtitle> = emptyList()
) : Parcelable

@Parcelize
data class StremioSubtitle(
    val id: String,
    val url: String,
    val lang: String,              // ISO 639-1 language code
    val subtitlesName: String? = null // Display name (e.g., "Portuguese (Brazil)")
) : Parcelable

/**
 * Stremio addon manifest structure (parsed from manifest.json).
 */
data class StremioManifest(
    val id: String,
    val name: String,
    val version: String = "",
    val description: String = "",
    val types: List<String> = emptyList(),
    val resources: List<Any> = emptyList(), // Can be strings or objects
    val catalogs: List<Any> = emptyList(),
    val behaviorHints: Map<String, Any> = emptyMap()
) {
    val resourceNames: List<String>
        get() = resources.map { resource ->
            when (resource) {
                is String -> resource
                is Map<*, *> -> resource["name"] as? String ?: ""
                else -> ""
            }
        }.filter { it.isNotEmpty() }

    val hasStreams: Boolean get() = "stream" in resourceNames
    val hasSubtitles: Boolean get() = "subtitles" in resourceNames
    val hasCatalog: Boolean get() = "catalog" in resourceNames
}
