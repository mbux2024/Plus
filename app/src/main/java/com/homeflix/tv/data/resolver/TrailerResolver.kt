package com.homeflix.tv.data.resolver

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.homeflix.tv.domain.model.InnerTubeClient
import com.homeflix.tv.domain.model.ResolvedTrailer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TrailerResolver"

/**
 * Resolves YouTube video IDs to direct stream URLs using the InnerTube API.
 * Tries multiple client types (ANDROID_VR → ANDROID → iOS) across candidates.
 * NuvioTV-style — no WebView IFrame player needed.
 */
@Singleton
class TrailerResolver @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {

    /**
     * Resolve a YouTube video ID to a direct playable stream URL.
     * @param youtubeId The YouTube video ID (e.g., "dQw4w9WgXcQ")
     * @return ResolvedTrailer with the best direct stream URL, or null if all clients fail
     */
    suspend fun resolve(youtubeId: String): Result<ResolvedTrailer> = withContext(Dispatchers.IO) {
        // Try each InnerTube client in order
        for (client in InnerTubeClient.entries) {
            try {
                val result = tryClient(youtubeId, client)
                if (result != null) {
                    Log.d(TAG, "Resolved trailer $youtubeId via ${client.clientName}")
                    return@withContext Result.success(result)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Client ${client.clientName} failed for $youtubeId: ${e.message}")
            }
        }
        Result.failure(Exception("All InnerTube clients failed for video $youtubeId"))
    }

    private fun tryClient(youtubeId: String, client: InnerTubeClient): ResolvedTrailer? {
        val url = "https://www.youtube.com/youtubei/v1/player?key=${client.apiKey}"

        val requestBody = gson.toJson(
            mapOf(
                "videoId" to youtubeId,
                "context" to mapOf(
                    "client" to mapOf(
                        "clientName" to client.clientName,
                        "clientVersion" to client.clientVersion,
                        "hl" to "en",
                        "gl" to "US"
                    )
                ),
                "playbackContext" to mapOf(
                    "contentPlaybackContext" to mapOf(
                        "html5Preference" to "HTML5_PREF_WANTS"
                    )
                )
            )
        )

        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .addHeader("User-Agent", client.userAgent)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val body = response.body?.string() ?: return null
        val json = gson.fromJson(body, JsonObject::class.java)

        // Check playability
        val playabilityStatus = json.getAsJsonObject("playabilityStatus")
        val status = playabilityStatus?.get("status")?.asString
        if (status != "OK") return null

        // Get streaming data
        val streamingData = json.getAsJsonObject("streamingData") ?: return null

        // Try adaptive formats first (better quality), then regular formats
        val formats = mutableListOf<JsonObject>()

        streamingData.getAsJsonArray("adaptiveFormats")?.forEach { element ->
            formats.add(element.asJsonObject)
        }
        streamingData.getAsJsonArray("formats")?.forEach { element ->
            formats.add(element.asJsonObject)
        }

        if (formats.isEmpty()) return null

        // Pick the best video format (prefer combined audio+video, then highest quality adaptive)
        val combinedFormats = formats.filter { fmt ->
            val mimeType = fmt.get("mimeType")?.asString ?: ""
            mimeType.contains("video/") && fmt.has("audioQuality")
        }

        val bestFormat = if (combinedFormats.isNotEmpty()) {
            // Prefer combined (progressive) format
            combinedFormats.maxByOrNull { fmt ->
                fmt.get("height")?.asInt ?: 0
            }
        } else {
            // Fall back to highest quality video-only adaptive format
            formats.filter { fmt ->
                val mimeType = fmt.get("mimeType")?.asString ?: ""
                mimeType.contains("video/")
            }.maxByOrNull { fmt ->
                fmt.get("height")?.asInt ?: 0
            }
        }

        bestFormat ?: return null

        val streamUrl = bestFormat.get("url")?.asString ?: return null
        val mimeType = bestFormat.get("mimeType")?.asString ?: ""
        val height = bestFormat.get("height")?.asInt ?: 0
        val quality = "${height}p"

        // Get video title
        val videoDetails = json.getAsJsonObject("videoDetails")
        val title = videoDetails?.get("title")?.asString ?: ""

        return ResolvedTrailer(
            youtubeId = youtubeId,
            title = title,
            streamUrl = streamUrl,
            mimeType = mimeType,
            quality = quality,
            isAdaptive = combinedFormats.isEmpty() // True if we're using adaptive (video-only)
        )
    }
}
