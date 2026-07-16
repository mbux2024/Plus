package com.homeflix.tv.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Configuration for a debrid service (TorBox or Real-Debrid).
 */
@Parcelize
data class DebridConfig(
    val service: DebridServiceType,
    val apiKey: String,
    val isConnected: Boolean = false,
    val username: String? = null,
    val expirationDate: String? = null
) : Parcelable

// ─── TorBox Models ───────────────────────────────────────────────────────────

/**
 * TorBox cache check response.
 * POST /torrents/checkcached with { hashes: [...] }
 */
data class TorBoxCacheCheckResponse(
    val success: Boolean,
    val data: Map<String, TorBoxCacheStatus> = emptyMap()
)

data class TorBoxCacheStatus(
    val name: String? = null,
    val size: Long = 0,
    val hash: String = ""
)

/**
 * TorBox create torrent response.
 * POST /torrents/createtorrent
 */
data class TorBoxCreateTorrentResponse(
    val success: Boolean,
    val data: TorBoxTorrentData? = null
)

data class TorBoxTorrentData(
    val id: Int,
    val hash: String,
    val name: String? = null,
    val size: Long = 0,
    val status: String? = null // "cached", "downloading", etc.
)

/**
 * TorBox request download link response.
 * GET /torrents/requestdl?torrent_id={id}&file_id={fileId}
 */
data class TorBoxRequestDlResponse(
    val success: Boolean,
    val data: String? = null // Direct download URL
)

/**
 * TorBox torrent info (for listing files).
 * GET /torrents/mylist
 */
data class TorBoxTorrentInfo(
    val id: Int,
    val hash: String,
    val name: String,
    val size: Long = 0,
    val status: String = "",
    val files: List<TorBoxFile> = emptyList()
)

data class TorBoxFile(
    val id: Int,
    val name: String,
    val size: Long = 0,
    val shortName: String? = null
)

// ─── Real-Debrid Models ──────────────────────────────────────────────────────

/**
 * Real-Debrid instant availability check.
 * GET /torrents/instantAvailability/{hash}
 */
data class RealDebridInstantResponse(
    // Map of hash -> list of file variants available
    val data: Map<String, List<RealDebridFileVariant>> = emptyMap()
)

data class RealDebridFileVariant(
    val files: Map<String, RealDebridFileInfo> = emptyMap()
)

data class RealDebridFileInfo(
    val filename: String,
    val filesize: Long = 0
)

/**
 * Real-Debrid add magnet response.
 * POST /torrents/addMagnet with magnet=...
 */
data class RealDebridAddMagnetResponse(
    val id: String,
    val uri: String? = null
)

/**
 * Real-Debrid torrent info.
 * GET /torrents/info/{id}
 */
data class RealDebridTorrentInfo(
    val id: String,
    val filename: String = "",
    val hash: String = "",
    val status: String = "",
    val files: List<RealDebridTorrentFile> = emptyList(),
    val links: List<String> = emptyList()
)

data class RealDebridTorrentFile(
    val id: Int,
    val path: String = "",
    val bytes: Long = 0,
    val selected: Int = 0 // 1 = selected
)

/**
 * Real-Debrid unrestrict link response.
 * POST /unrestrict/link with link=...
 */
data class RealDebridUnrestrictResponse(
    val id: String,
    val filename: String = "",
    val mimeType: String = "",
    val filesize: Long = 0,
    val download: String = "",  // Direct download/stream URL
    val streamable: Int = 0
)

/**
 * Real-Debrid user info.
 * GET /user
 */
data class RealDebridUserInfo(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val type: String = "",      // "premium"
    val expiration: String = "" // ISO date
)
