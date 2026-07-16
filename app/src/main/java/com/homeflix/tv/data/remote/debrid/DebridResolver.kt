package com.homeflix.tv.data.remote.debrid

import android.util.Log
import com.homeflix.tv.domain.model.DebridServiceType
import com.homeflix.tv.domain.repository.SettingsRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DebridResolver"

/**
 * Resolves a torrent infoHash to a direct stream URL via debrid services.
 * Strategy: TorBox first (checkcached instant → createtorrent → requestdl),
 * then Real-Debrid as fallback (addMagnet → selectFiles → unrestrict).
 * Whichever has the release cached plays instantly.
 */
@Singleton
class DebridResolver @Inject constructor(
    private val torBoxApi: TorBoxApiService,
    private val realDebridApi: RealDebridApiService,
    private val settingsRepository: SettingsRepository
) {

    // ─── Main Resolution ─────────────────────────────────────────────────

    /**
     * Resolve an infoHash to a direct stream URL.
     * Tries TorBox first, then Real-Debrid.
     *
     * @param infoHash Torrent info hash
     * @param fileIndex Optional file index within the torrent (for series)
     * @return Pair of (direct URL, which service resolved it)
     */
    suspend fun resolve(
        infoHash: String,
        fileIndex: Int? = null
    ): Result<Pair<String, DebridServiceType>> {
        val settings = settingsRepository.getSettings()
        val torboxKey = settings.torboxApiKey
        val rdKey = settings.realDebridApiKey

        // Try TorBox first
        if (torboxKey.isNotBlank()) {
            val torboxResult = resolveTorBox(infoHash, fileIndex, torboxKey)
            if (torboxResult.isSuccess) {
                return Result.success(torboxResult.getOrThrow() to DebridServiceType.TORBOX)
            }
            Log.w(TAG, "TorBox resolve failed, trying Real-Debrid...")
        }

        // Try Real-Debrid as fallback
        if (rdKey.isNotBlank()) {
            val rdResult = resolveRealDebrid(infoHash, fileIndex, rdKey)
            if (rdResult.isSuccess) {
                return Result.success(rdResult.getOrThrow() to DebridServiceType.REAL_DEBRID)
            }
            Log.w(TAG, "Real-Debrid resolve also failed")
        }

        // Neither service available or both failed
        return if (torboxKey.isBlank() && rdKey.isBlank()) {
            Result.failure(Exception("No debrid service configured. Add a TorBox or Real-Debrid API key in Settings."))
        } else {
            Result.failure(Exception("Failed to resolve stream from any debrid service"))
        }
    }

    // ─── Cache Check (batch) ─────────────────────────────────────────────

    /**
     * Check which hashes are cached (instant) on TorBox.
     * @return Set of cached hashes
     */
    suspend fun checkTorBoxCached(hashes: List<String>): Set<String> {
        val settings = settingsRepository.getSettings()
        val apiKey = settings.torboxApiKey
        if (apiKey.isBlank() || hashes.isEmpty()) return emptySet()

        return try {
            val response = torBoxApi.checkCached(
                authHeader = "Bearer $apiKey",
                hashes = hashes.joinToString(",")
            )
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.map { it.hash.lowercase() }?.toSet() ?: emptySet()
            } else {
                emptySet()
            }
        } catch (e: Exception) {
            Log.e(TAG, "TorBox cache check error: ${e.message}")
            emptySet()
        }
    }

    /**
     * Check which hashes are cached on Real-Debrid.
     * @return Set of cached hashes
     */
    suspend fun checkRealDebridCached(hashes: List<String>): Set<String> {
        val settings = settingsRepository.getSettings()
        val apiKey = settings.realDebridApiKey
        if (apiKey.isBlank() || hashes.isEmpty()) return emptySet()

        return try {
            // RD expects hashes separated by slashes in the path
            val hashPath = hashes.joinToString("/")
            val response = realDebridApi.checkInstantAvailability(
                authHeader = "Bearer $apiKey",
                hashes = hashPath
            )
            if (response.isSuccessful) {
                val data = response.body() ?: emptyMap()
                // A hash is cached if it has at least one "rd" entry
                data.filter { (_, value) ->
                    value.rd?.isNotEmpty() == true
                }.keys.map { it.lowercase() }.toSet()
            } else {
                emptySet()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Real-Debrid cache check error: ${e.message}")
            emptySet()
        }
    }

    // ─── TorBox Resolution Flow ──────────────────────────────────────────

    /**
     * TorBox flow: createtorrent → wait for "completed/cached" → requestdl
     */
    private suspend fun resolveTorBox(
        infoHash: String,
        fileIndex: Int?,
        apiKey: String
    ): Result<String> {
        return try {
            val authHeader = "Bearer $apiKey"
            val magnet = "magnet:?xt=urn:btih:$infoHash"

            // Step 1: Create the torrent (if already cached, this returns immediately)
            Log.d(TAG, "TorBox: Creating torrent for $infoHash")
            val createResponse = torBoxApi.createTorrent(
                authHeader = authHeader,
                magnet = magnet
            )

            if (!createResponse.isSuccessful || createResponse.body()?.success != true) {
                val error = createResponse.body()?.detail ?: "Failed to create torrent"
                return Result.failure(Exception("TorBox createtorrent failed: $error"))
            }

            val torrentId = createResponse.body()?.data?.torrentId
                ?: return Result.failure(Exception("TorBox: No torrent ID returned"))

            Log.d(TAG, "TorBox: Torrent created with ID $torrentId")

            // Step 2: Wait for torrent to be ready (usually instant if cached)
            val torrentInfo = waitForTorBoxReady(authHeader, torrentId)
                ?: return Result.failure(Exception("TorBox: Torrent not ready after timeout"))

            // Step 3: Find the right file
            val targetFile = if (fileIndex != null && fileIndex < torrentInfo.files.size) {
                torrentInfo.files[fileIndex]
            } else {
                // Pick the largest video file
                torrentInfo.files
                    .filter { isVideoFile(it.name) }
                    .maxByOrNull { it.size }
                    ?: torrentInfo.files.maxByOrNull { it.size }
                    ?: return Result.failure(Exception("TorBox: No files in torrent"))
            }

            // Step 4: Request download link
            Log.d(TAG, "TorBox: Requesting download link for file ${targetFile.id}")
            val dlResponse = torBoxApi.requestDownloadLink(
                authHeader = authHeader,
                torrentId = torrentId,
                fileId = targetFile.id
            )

            if (!dlResponse.isSuccessful || dlResponse.body()?.success != true) {
                return Result.failure(Exception("TorBox requestdl failed"))
            }

            val streamUrl = dlResponse.body()?.data
                ?: return Result.failure(Exception("TorBox: No download URL returned"))

            Log.d(TAG, "TorBox: Stream URL resolved successfully")
            Result.success(streamUrl)
        } catch (e: Exception) {
            Log.e(TAG, "TorBox resolve error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun waitForTorBoxReady(
        authHeader: String,
        torrentId: Int,
        maxAttempts: Int = 15,
        delayMs: Long = 2000
    ): TorBoxTorrentInfoDto? {
        repeat(maxAttempts) { attempt ->
            try {
                val response = torBoxApi.getTorrentInfo(authHeader, torrentId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val info = response.body()?.data
                    if (info != null) {
                        val state = info.downloadState.lowercase()
                        if (state == "completed" || state == "cached" || state == "uploading") {
                            return info
                        }
                        Log.d(TAG, "TorBox: Waiting for torrent... state=$state (attempt ${attempt + 1})")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "TorBox: Error checking torrent status: ${e.message}")
            }
            delay(delayMs)
        }
        return null
    }

    // ─── Real-Debrid Resolution Flow ─────────────────────────────────────

    /**
     * Real-Debrid flow: addMagnet → selectFiles → info (get links) → unrestrict
     */
    private suspend fun resolveRealDebrid(
        infoHash: String,
        fileIndex: Int?,
        apiKey: String
    ): Result<String> {
        return try {
            val authHeader = "Bearer $apiKey"
            val magnet = "magnet:?xt=urn:btih:$infoHash"

            // Step 1: Add magnet
            Log.d(TAG, "RD: Adding magnet for $infoHash")
            val addResponse = realDebridApi.addMagnet(
                authHeader = authHeader,
                magnet = magnet
            )

            if (!addResponse.isSuccessful) {
                return Result.failure(Exception("RD addMagnet failed: HTTP ${addResponse.code()}"))
            }

            val torrentId = addResponse.body()?.id
                ?: return Result.failure(Exception("RD: No torrent ID returned"))

            Log.d(TAG, "RD: Magnet added with ID $torrentId")

            // Step 2: Get torrent info to find files
            val infoResponse = realDebridApi.getTorrentInfo(authHeader, torrentId)
            if (!infoResponse.isSuccessful) {
                return Result.failure(Exception("RD getTorrentInfo failed"))
            }

            val torrentInfo = infoResponse.body()
                ?: return Result.failure(Exception("RD: Empty torrent info"))

            // Step 3: Select files (if status is "waiting_files_selection")
            if (torrentInfo.status == "waiting_files_selection") {
                val filesToSelect = if (fileIndex != null) {
                    // Select specific file
                    val targetFile = torrentInfo.files.getOrNull(fileIndex)
                    targetFile?.id?.toString() ?: "all"
                } else {
                    // Select largest video file
                    val videoFiles = torrentInfo.files.filter { isVideoFile(it.path) }
                    val largest = videoFiles.maxByOrNull { it.bytes }
                        ?: torrentInfo.files.maxByOrNull { it.bytes }
                    largest?.id?.toString() ?: "all"
                }

                Log.d(TAG, "RD: Selecting files: $filesToSelect")
                realDebridApi.selectFiles(authHeader, torrentId, filesToSelect)

                // Brief wait for RD to process
                delay(1000)
            }

            // Step 4: Get updated info with links
            val updatedInfo = waitForRealDebridReady(authHeader, torrentId)
                ?: return Result.failure(Exception("RD: Torrent not ready after timeout"))

            if (updatedInfo.links.isEmpty()) {
                return Result.failure(Exception("RD: No download links available"))
            }

            // Step 5: Unrestrict the first link to get direct URL
            val link = updatedInfo.links.first()
            Log.d(TAG, "RD: Unrestricting link...")
            val unrestrictResponse = realDebridApi.unrestrictLink(authHeader, link)

            if (!unrestrictResponse.isSuccessful) {
                return Result.failure(Exception("RD unrestrict failed: HTTP ${unrestrictResponse.code()}"))
            }

            val streamUrl = unrestrictResponse.body()?.download
                ?: return Result.failure(Exception("RD: No stream URL in unrestrict response"))

            // Cleanup: delete the torrent from RD account
            try {
                realDebridApi.deleteTorrent(authHeader, torrentId)
            } catch (e: Exception) {
                // Non-critical — don't fail the resolve
                Log.w(TAG, "RD: Failed to cleanup torrent: ${e.message}")
            }

            Log.d(TAG, "RD: Stream URL resolved successfully")
            Result.success(streamUrl)
        } catch (e: Exception) {
            Log.e(TAG, "RD resolve error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun waitForRealDebridReady(
        authHeader: String,
        torrentId: String,
        maxAttempts: Int = 15,
        delayMs: Long = 2000
    ): RdTorrentInfoDto? {
        repeat(maxAttempts) { attempt ->
            try {
                val response = realDebridApi.getTorrentInfo(authHeader, torrentId)
                if (response.isSuccessful) {
                    val info = response.body()
                    if (info != null && info.status == "downloaded" && info.links.isNotEmpty()) {
                        return info
                    }
                    Log.d(TAG, "RD: Waiting... status=${info?.status}, progress=${info?.progress}% (attempt ${attempt + 1})")
                }
            } catch (e: Exception) {
                Log.w(TAG, "RD: Error checking status: ${e.message}")
            }
            delay(delayMs)
        }
        return null
    }

    // ─── User Verification ───────────────────────────────────────────────

    /**
     * Verify TorBox API key and get account info.
     */
    suspend fun verifyTorBoxKey(apiKey: String): Result<TorBoxUserDto> {
        return try {
            val response = torBoxApi.getUserInfo("Bearer $apiKey")
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()?.data
                    ?: return Result.failure(Exception("Empty user data"))
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid TorBox API key"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify Real-Debrid API key and get account info.
     */
    suspend fun verifyRealDebridKey(apiKey: String): Result<RdUserDto> {
        return try {
            val response = realDebridApi.getUserInfo("Bearer $apiKey")
            if (response.isSuccessful) {
                val user = response.body()
                    ?: return Result.failure(Exception("Empty user data"))
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid Real-Debrid API key"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private fun isVideoFile(filename: String): Boolean {
        val videoExtensions = listOf(
            ".mkv", ".mp4", ".avi", ".mov", ".wmv", ".flv",
            ".webm", ".m4v", ".mpg", ".mpeg", ".ts", ".m2ts"
        )
        return videoExtensions.any { filename.lowercase().endsWith(it) }
    }
}
