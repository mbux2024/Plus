package com.homeflix.tv.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A resolved stream source from a Stremio addon.
 * Contains parsed quality badges and debrid availability info.
 */
@Parcelize
data class StreamSource(
    val title: String,
    val url: String? = null,           // Direct stream URL (after debrid resolution)
    val infoHash: String? = null,       // Torrent info hash (before debrid resolution)
    val fileIndex: Int? = null,         // File index in torrent (for series)
    val addonName: String = "",         // Which addon provided this source
    val addonId: String = "",
    // Parsed quality badges
    val resolution: StreamResolution = StreamResolution.UNKNOWN,
    val videoCodec: VideoCodec = VideoCodec.UNKNOWN,
    val hdrType: HdrType = HdrType.NONE,
    val audioCodec: AudioCodec = AudioCodec.UNKNOWN,
    val audioChannels: AudioChannels = AudioChannels.UNKNOWN,
    val sourceType: SourceType = SourceType.UNKNOWN,
    // Size and seeders
    val sizeBytes: Long = 0L,
    val seeders: Int = 0,
    // Debrid info
    val debridService: DebridServiceType = DebridServiceType.NONE,
    val isCached: Boolean = false,     // ⚡ INSTANT marker
    // Behavior flags
    val behaviorHints: StreamBehaviorHints = StreamBehaviorHints()
) : Parcelable {

    val sizeDisplay: String
        get() = when {
            sizeBytes >= 1_073_741_824L -> String.format("%.1f GB", sizeBytes / 1_073_741_824.0)
            sizeBytes >= 1_048_576L -> String.format("%.0f MB", sizeBytes / 1_048_576.0)
            sizeBytes > 0 -> String.format("%.0f KB", sizeBytes / 1024.0)
            else -> ""
        }

    val qualityScore: Int
        get() {
            var score = 0
            score += resolution.score
            score += hdrType.score
            score += audioCodec.score
            score += sourceType.score
            if (isCached) score += 100 // Strongly prefer cached
            // De-prioritise CAM/TS
            if (sourceType == SourceType.CAM || sourceType == SourceType.TS) score -= 500
            return score
        }
}

@Parcelize
data class StreamBehaviorHints(
    val bingeGroup: String? = null,
    val filename: String? = null,
    val videoSize: Long? = null
) : Parcelable

enum class StreamResolution(val label: String, val score: Int) {
    UHD_4K("4K", 40),
    QHD_1440P("1440p", 35),
    FHD_1080P("1080p", 30),
    HD_720P("720p", 20),
    SD_480P("480p", 10),
    UNKNOWN("", 0);
}

enum class VideoCodec(val label: String) {
    AV1("AV1"),
    HEVC("HEVC"),
    H264("H.264"),
    VP9("VP9"),
    UNKNOWN("");
}

enum class HdrType(val label: String, val score: Int) {
    DOLBY_VISION("DV", 15),
    HDR10_PLUS("HDR10+", 12),
    HDR10("HDR10", 10),
    HLG("HLG", 8),
    HDR("HDR", 8),
    NONE("", 0);
}

enum class AudioCodec(val label: String, val score: Int) {
    DOLBY_ATMOS("Atmos", 20),
    DOLBY_TRUEHD("TrueHD", 18),
    DTS_X("DTS:X", 17),
    DTS_HD_MA("DTS-HD MA", 16),
    DTS_HD("DTS-HD", 14),
    DOLBY_DIGITAL_PLUS("DD+", 12),
    DTS("DTS", 10),
    DOLBY_DIGITAL("DD", 8),
    AAC("AAC", 5),
    UNKNOWN("", 0);
}

enum class AudioChannels(val label: String) {
    CH_7_1("7.1"),
    CH_5_1("5.1"),
    CH_2_0("2.0"),
    UNKNOWN("");
}

enum class SourceType(val label: String, val score: Int) {
    REMUX("REMUX", 25),
    BLURAY("BluRay", 20),
    WEB_DL("WEB-DL", 18),
    WEB_RIP("WEBRip", 15),
    HDTV("HDTV", 10),
    TS("TS", -50),
    CAM("CAM", -100),
    UNKNOWN("", 0);
}

enum class DebridServiceType(val label: String) {
    TORBOX("TorBox"),
    REAL_DEBRID("RD"),
    NONE("");
}
