package com.homeflix.tv.domain.model

/**
 * Resolved trailer stream from InnerTube extractor.
 * YouTube video ID → direct stream URL (no WebView needed).
 */
data class ResolvedTrailer(
    val youtubeId: String,
    val title: String = "",
    val streamUrl: String,         // Direct playable URL
    val mimeType: String = "",
    val quality: String = "",      // "1080p", "720p", etc.
    val isAdaptive: Boolean = false
)

/**
 * InnerTube client types for YouTube stream extraction.
 * Tried in order: ANDROID_VR → ANDROID → IOS
 */
enum class InnerTubeClient(
    val clientName: String,
    val clientVersion: String,
    val apiKey: String,
    val userAgent: String
) {
    ANDROID_VR(
        clientName = "ANDROID_VR",
        clientVersion = "1.57.29",
        apiKey = "AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w",
        userAgent = "com.google.android.apps.youtube.vr.oculus/1.57.29 (Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1) gzip"
    ),
    ANDROID(
        clientName = "ANDROID",
        clientVersion = "19.09.37",
        apiKey = "AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w",
        userAgent = "com.google.android.youtube/19.09.37 (Linux; U; Android 11) gzip"
    ),
    IOS(
        clientName = "IOS",
        clientVersion = "19.09.3",
        apiKey = "AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc",
        userAgent = "com.google.ios.youtube/19.09.3 (iPhone14,3; U; CPU iOS 15_6 like Mac OS X)"
    );
}
