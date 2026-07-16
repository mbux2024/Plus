package com.homeflix.tv.data.resolver

import com.homeflix.tv.domain.model.*

/**
 * Parses quality badges from torrent release names / addon stream titles.
 *
 * Examples of release names:
 * - "The.Movie.2024.2160p.UHD.BluRay.REMUX.DV.HDR.TrueHD.7.1.Atmos-GROUP"
 * - "Movie.2024.1080p.WEB-DL.DD+5.1.H.264-GROUP"
 * - "Show.S01E01.720p.HDTV.x264-GROUP"
 */
object QualityParser {

    /**
     * Parse all quality attributes from a release/stream title.
     */
    fun parse(title: String): ParsedQuality {
        val upper = title.uppercase()
        return ParsedQuality(
            resolution = parseResolution(upper),
            videoCodec = parseVideoCodec(upper),
            hdrType = parseHdr(upper),
            audioCodec = parseAudioCodec(upper),
            audioChannels = parseAudioChannels(upper),
            sourceType = parseSourceType(upper),
            sizeBytes = parseSize(title)
        )
    }

    // ─── Resolution ──────────────────────────────────────────────────────

    private fun parseResolution(title: String): StreamResolution {
        return when {
            title.contains("2160P") || title.contains("4K") || title.contains("UHD") ->
                StreamResolution.UHD_4K
            title.contains("1440P") || title.contains("QHD") ->
                StreamResolution.QHD_1440P
            title.contains("1080P") || title.contains("FHD") ->
                StreamResolution.FHD_1080P
            title.contains("720P") || title.contains("HD") && !title.contains("UHD") ->
                StreamResolution.HD_720P
            title.contains("480P") || title.contains("SD") ->
                StreamResolution.SD_480P
            else -> StreamResolution.UNKNOWN
        }
    }

    // ─── Video Codec ─────────────────────────────────────────────────────

    private fun parseVideoCodec(title: String): VideoCodec {
        return when {
            title.contains("AV1") -> VideoCodec.AV1
            title.contains("HEVC") || title.contains("H.265") || title.contains("H265") ||
                    title.contains("X265") || title.contains("X.265") -> VideoCodec.HEVC
            title.contains("H.264") || title.contains("H264") ||
                    title.contains("X264") || title.contains("X.264") || title.contains("AVC") -> VideoCodec.H264
            title.contains("VP9") -> VideoCodec.VP9
            else -> VideoCodec.UNKNOWN
        }
    }

    // ─── HDR Type ────────────────────────────────────────────────────────

    private fun parseHdr(title: String): HdrType {
        return when {
            // Check Dolby Vision first (most specific)
            title.contains("DOLBY VISION") || title.contains("DOLBY.VISION") ||
                    title.contains("DV") && (title.contains("HDR") || title.contains("2160P")) ||
                    title.contains("DOVI") -> HdrType.DOLBY_VISION
            // HDR10+
            title.contains("HDR10+") || title.contains("HDR10PLUS") ||
                    title.contains("HDR10 PLUS") -> HdrType.HDR10_PLUS
            // HDR10
            title.contains("HDR10") -> HdrType.HDR10
            // HLG
            title.contains("HLG") -> HdrType.HLG
            // Generic HDR
            title.contains("HDR") -> HdrType.HDR
            else -> HdrType.NONE
        }
    }

    // ─── Audio Codec ─────────────────────────────────────────────────────

    private fun parseAudioCodec(title: String): AudioCodec {
        return when {
            // Dolby Atmos (check first — it's usually combined with TrueHD or DD+)
            title.contains("ATMOS") -> AudioCodec.DOLBY_ATMOS
            // TrueHD
            title.contains("TRUEHD") || title.contains("TRUE.HD") ||
                    title.contains("TRUE HD") -> AudioCodec.DOLBY_TRUEHD
            // DTS:X
            title.contains("DTS:X") || title.contains("DTS-X") ||
                    title.contains("DTSX") -> AudioCodec.DTS_X
            // DTS-HD MA
            title.contains("DTS-HD.MA") || title.contains("DTS-HD MA") ||
                    title.contains("DTSHD.MA") || title.contains("DTS HD MA") -> AudioCodec.DTS_HD_MA
            // DTS-HD
            title.contains("DTS-HD") || title.contains("DTS HD") ||
                    title.contains("DTSHD") -> AudioCodec.DTS_HD
            // DD+ / E-AC3
            title.contains("DD+") || title.contains("DDP") ||
                    title.contains("EAC3") || title.contains("E-AC-3") ||
                    title.contains("E-AC3") || title.contains("DDPLUS") -> AudioCodec.DOLBY_DIGITAL_PLUS
            // DTS
            title.contains("DTS") && !title.contains("DTS-HD") && !title.contains("DTSX") ->
                AudioCodec.DTS
            // DD / AC3
            title.contains("DD") && !title.contains("DD+") && !title.contains("DDP") ||
                    title.contains("AC3") || title.contains("AC-3") ||
                    title.contains("DOLBY DIGITAL") && !title.contains("PLUS") -> AudioCodec.DOLBY_DIGITAL
            // AAC
            title.contains("AAC") -> AudioCodec.AAC
            else -> AudioCodec.UNKNOWN
        }
    }

    // ─── Audio Channels ──────────────────────────────────────────────────

    private fun parseAudioChannels(title: String): AudioChannels {
        return when {
            title.contains("7.1") -> AudioChannels.CH_7_1
            title.contains("5.1") -> AudioChannels.CH_5_1
            title.contains("2.0") || title.contains("STEREO") -> AudioChannels.CH_2_0
            else -> AudioChannels.UNKNOWN
        }
    }

    // ─── Source Type ─────────────────────────────────────────────────────

    private fun parseSourceType(title: String): SourceType {
        return when {
            title.contains("REMUX") -> SourceType.REMUX
            title.contains("BLURAY") || title.contains("BLU-RAY") ||
                    title.contains("BDRIP") || title.contains("BDREMUX") -> SourceType.BLURAY
            title.contains("WEB-DL") || title.contains("WEBDL") ||
                    title.contains("WEB DL") -> SourceType.WEB_DL
            title.contains("WEBRIP") || title.contains("WEB-RIP") ||
                    title.contains("WEB RIP") -> SourceType.WEB_RIP
            title.contains("HDTV") || title.contains("PDTV") -> SourceType.HDTV
            title.contains("TELESYNC") || title.contains(" TS ") ||
                    title.contains(".TS.") || title.contains("HDTS") -> SourceType.TS
            title.contains("CAM") || title.contains("HDCAM") ||
                    title.contains("CAMRIP") -> SourceType.CAM
            else -> SourceType.UNKNOWN
        }
    }

    // ─── File Size ───────────────────────────────────────────────────────

    private fun parseSize(title: String): Long {
        // Match patterns like "4.2 GB", "1.5GB", "850 MB", "850MB"
        val gbPattern = Regex("""(\d+\.?\d*)\s*GB""", RegexOption.IGNORE_CASE)
        val mbPattern = Regex("""(\d+\.?\d*)\s*MB""", RegexOption.IGNORE_CASE)

        gbPattern.find(title)?.let { match ->
            val gb = match.groupValues[1].toDoubleOrNull() ?: return 0L
            return (gb * 1_073_741_824).toLong()
        }

        mbPattern.find(title)?.let { match ->
            val mb = match.groupValues[1].toDoubleOrNull() ?: return 0L
            return (mb * 1_048_576).toLong()
        }

        return 0L
    }

    /**
     * Extract seeders count from stream title if present.
     * Common format: "👤 123" or "Seeds: 123"
     */
    fun parseSeeders(title: String): Int {
        val patterns = listOf(
            Regex("""👤\s*(\d+)"""),
            Regex("""[Ss]eeds?:?\s*(\d+)"""),
            Regex("""(\d+)\s*[Ss]eeds?""")
        )
        for (pattern in patterns) {
            pattern.find(title)?.let { match ->
                return match.groupValues[1].toIntOrNull() ?: 0
            }
        }
        return 0
    }
}

data class ParsedQuality(
    val resolution: StreamResolution = StreamResolution.UNKNOWN,
    val videoCodec: VideoCodec = VideoCodec.UNKNOWN,
    val hdrType: HdrType = HdrType.NONE,
    val audioCodec: AudioCodec = AudioCodec.UNKNOWN,
    val audioChannels: AudioChannels = AudioChannels.UNKNOWN,
    val sourceType: SourceType = SourceType.UNKNOWN,
    val sizeBytes: Long = 0L
)
