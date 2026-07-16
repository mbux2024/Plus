package com.homeflix.tv.data.remote.debrid

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

/**
 * Real-Debrid API v1.0 Retrofit service.
 * Base URL: https://api.real-debrid.com/rest/1.0/
 * Auth: Bearer token header (API key from Settings).
 *
 * Flow for instant playback:
 * 1. instantAvailability — check if hash is cached
 * 2. addMagnet — add the torrent
 * 3. selectFiles — select which files to download
 * 4. torrents/info — get download links
 * 5. unrestrict/link — convert to direct stream URL
 */
interface RealDebridApiService {

    // ─── Instant Availability ────────────────────────────────────────────

    /**
     * Check instant availability for torrent hashes.
     * GET /torrents/instantAvailability/{hash1}/{hash2}/...
     *
     * Note: RD limits to ~100 hashes per request.
     * Response is a map: { "hash": { "rd": [{"fileid": {...}}] } }
     */
    @GET("torrents/instantAvailability/{hashes}")
    suspend fun checkInstantAvailability(
        @Header("Authorization") authHeader: String,
        @Path("hashes", encoded = true) hashes: String  // slash-separated
    ): Response<Map<String, RdInstantData>>

    // ─── Add Magnet ──────────────────────────────────────────────────────

    /**
     * Add a magnet link to Real-Debrid.
     * POST /torrents/addMagnet
     */
    @FormUrlEncoded
    @POST("torrents/addMagnet")
    suspend fun addMagnet(
        @Header("Authorization") authHeader: String,
        @Field("magnet") magnet: String
    ): Response<RdAddMagnetResponse>

    // ─── Select Files ────────────────────────────────────────────────────

    /**
     * Select which files to download from a torrent.
     * POST /torrents/selectFiles/{id}
     * @param files Comma-separated file IDs, or "all"
     */
    @FormUrlEncoded
    @POST("torrents/selectFiles/{id}")
    suspend fun selectFiles(
        @Header("Authorization") authHeader: String,
        @Path("id") torrentId: String,
        @Field("files") files: String = "all"
    ): Response<Unit>

    // ─── Torrent Info ────────────────────────────────────────────────────

    /**
     * Get torrent info including download links.
     * GET /torrents/info/{id}
     */
    @GET("torrents/info/{id}")
    suspend fun getTorrentInfo(
        @Header("Authorization") authHeader: String,
        @Path("id") torrentId: String
    ): Response<RdTorrentInfoDto>

    // ─── Unrestrict Link ─────────────────────────────────────────────────

    /**
     * Unrestrict a hoster link to get a direct download/stream URL.
     * POST /unrestrict/link
     */
    @FormUrlEncoded
    @POST("unrestrict/link")
    suspend fun unrestrictLink(
        @Header("Authorization") authHeader: String,
        @Field("link") link: String
    ): Response<RdUnrestrictResponse>

    // ─── User Info ───────────────────────────────────────────────────────

    /**
     * Get user info (for verifying API key connection).
     * GET /user
     */
    @GET("user")
    suspend fun getUserInfo(
        @Header("Authorization") authHeader: String
    ): Response<RdUserDto>

    // ─── Torrent List ────────────────────────────────────────────────────

    /**
     * Get user's torrent list.
     * GET /torrents
     */
    @GET("torrents")
    suspend fun getTorrents(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int = 100
    ): Response<List<RdTorrentInfoDto>>

    // ─── Delete Torrent ──────────────────────────────────────────────────

    /**
     * Delete a torrent from Real-Debrid.
     * DELETE /torrents/delete/{id}
     */
    @DELETE("torrents/delete/{id}")
    suspend fun deleteTorrent(
        @Header("Authorization") authHeader: String,
        @Path("id") torrentId: String
    ): Response<Unit>
}

// ─── Real-Debrid DTOs ────────────────────────────────────────────────────────

/**
 * Instant availability data per hash.
 * Structure: { "rd": [ { "fileId": { filename, filesize } }, ... ] }
 */
data class RdInstantData(
    val rd: List<Map<String, RdInstantFile>>? = null
)

data class RdInstantFile(
    val filename: String = "",
    val filesize: Long = 0
)

data class RdAddMagnetResponse(
    val id: String,
    val uri: String? = null
)

data class RdTorrentInfoDto(
    val id: String,
    val filename: String = "",
    val hash: String = "",
    val bytes: Long = 0,
    val status: String = "",      // "downloaded", "waiting_files_selection", etc.
    val files: List<RdTorrentFileDto> = emptyList(),
    val links: List<String> = emptyList(),
    val progress: Int = 0,        // 0-100
    val speed: Long = 0,
    val seeders: Int = 0
)

data class RdTorrentFileDto(
    val id: Int,
    val path: String = "",
    val bytes: Long = 0,
    val selected: Int = 0         // 1 = selected for download
)

data class RdUnrestrictResponse(
    val id: String,
    val filename: String = "",
    @SerializedName("mimeType")
    val mimeType: String = "",
    val filesize: Long = 0,
    val download: String = "",    // Direct download/stream URL
    val streamable: Int = 0       // 1 = can be streamed
)

data class RdUserDto(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val type: String = "",         // "premium"
    val expiration: String = "",   // ISO date
    val points: Int = 0
)
