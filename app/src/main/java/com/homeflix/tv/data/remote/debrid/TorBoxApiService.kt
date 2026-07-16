package com.homeflix.tv.data.remote.debrid

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

/**
 * TorBox API v1 Retrofit service.
 * Base URL: https://api.torbox.app/v1/api/
 * Auth: Bearer token header (API key from Settings).
 *
 * Flow for instant playback:
 * 1. checkcached — verify hash is cached (instant availability)
 * 2. createtorrent — add the torrent (magnet or hash)
 * 3. requestdl — get direct download/stream URL
 */
interface TorBoxApiService {

    // ─── Cache Check ─────────────────────────────────────────────────────

    /**
     * Check if torrent hashes are cached (instant availability).
     * GET /torrents/checkcached?hash={hash1,hash2,...}&format=list
     */
    @GET("torrents/checkcached")
    suspend fun checkCached(
        @Header("Authorization") authHeader: String,
        @Query("hash") hashes: String,     // Comma-separated hashes
        @Query("format") format: String = "list"
    ): Response<TorBoxApiResponse<List<TorBoxCachedItem>>>

    // ─── Create Torrent ──────────────────────────────────────────────────

    /**
     * Create/add a torrent from magnet link.
     * POST /torrents/createtorrent
     */
    @FormUrlEncoded
    @POST("torrents/createtorrent")
    suspend fun createTorrent(
        @Header("Authorization") authHeader: String,
        @Field("magnet") magnet: String,
        @Field("seed") seed: Int = 1,          // Auto-seed ratio
        @Field("name") name: String? = null
    ): Response<TorBoxApiResponse<TorBoxCreateData>>

    // ─── Request Download Link ───────────────────────────────────────────

    /**
     * Get a direct download/stream link for a torrent file.
     * GET /torrents/requestdl?torrent_id={id}&file_id={fileId}&zip=false
     */
    @GET("torrents/requestdl")
    suspend fun requestDownloadLink(
        @Header("Authorization") authHeader: String,
        @Query("torrent_id") torrentId: Int,
        @Query("file_id") fileId: Int,
        @Query("zip_link") zipLink: Boolean = false
    ): Response<TorBoxApiResponse<String>> // data = direct URL string

    // ─── Torrent Info ────────────────────────────────────────────────────

    /**
     * Get info about a specific torrent (files list, status).
     * GET /torrents/mylist?id={torrentId}
     */
    @GET("torrents/mylist")
    suspend fun getTorrentInfo(
        @Header("Authorization") authHeader: String,
        @Query("id") torrentId: Int
    ): Response<TorBoxApiResponse<TorBoxTorrentInfoDto>>

    /**
     * Get the torrent list to find a torrent by hash.
     * GET /torrents/mylist
     */
    @GET("torrents/mylist")
    suspend fun getMyTorrents(
        @Header("Authorization") authHeader: String
    ): Response<TorBoxApiResponse<List<TorBoxTorrentInfoDto>>>

    // ─── User Info ───────────────────────────────────────────────────────

    /**
     * Get user account info (for verifying API key connection).
     * GET /user/me
     */
    @GET("user/me")
    suspend fun getUserInfo(
        @Header("Authorization") authHeader: String
    ): Response<TorBoxApiResponse<TorBoxUserDto>>
}

// ─── TorBox API Response Wrapper ─────────────────────────────────────────────

data class TorBoxApiResponse<T>(
    val success: Boolean,
    val error: String? = null,
    val detail: String? = null,
    val data: T? = null
)

// ─── TorBox DTOs ─────────────────────────────────────────────────────────────

data class TorBoxCachedItem(
    val hash: String,
    val name: String? = null,
    val size: Long = 0
)

data class TorBoxCreateData(
    @SerializedName("torrent_id")
    val torrentId: Int,
    val hash: String? = null,
    val name: String? = null
)

data class TorBoxTorrentInfoDto(
    val id: Int,
    val hash: String,
    val name: String = "",
    val size: Long = 0,
    @SerializedName("download_state")
    val downloadState: String = "",   // "completed", "downloading", "cached"
    @SerializedName("download_speed")
    val downloadSpeed: Long = 0,
    val seeds: Int = 0,
    val files: List<TorBoxFileDto> = emptyList(),
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class TorBoxFileDto(
    val id: Int,
    val name: String,
    val size: Long = 0,
    @SerializedName("short_name")
    val shortName: String? = null,
    @SerializedName("mime_type")
    val mimeType: String? = null
)

data class TorBoxUserDto(
    val id: Int = 0,
    val email: String = "",
    val plan: Int = 0,             // 0=free, 1=essential, 2=pro, 3=standard
    @SerializedName("total_downloaded")
    val totalDownloaded: Long = 0,
    @SerializedName("customer")
    val customer: String? = null,
    @SerializedName("is_subscribed")
    val isSubscribed: Boolean = false,
    @SerializedName("premium_expires_at")
    val premiumExpiresAt: String? = null
)
