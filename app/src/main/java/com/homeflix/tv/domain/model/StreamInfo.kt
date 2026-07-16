package com.homeflix.tv.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * COMPATIBILITY STUB — Old StreamInfo models.
 * Used by VideoPlayer.kt for subtitle/audio track display.
 * TODO: Remove once VideoPlayer is migrated to new player architecture.
 */

@Parcelize
data class SubtitleTrack(
    val id: Int = 0,
    val mediaId: Int = 0,
    val streamIndex: Int = 0,
    val language: String = "",
    val title: String? = null,
    val codecName: String = "",
    val filePath: String? = null,
    val format: String = "",
    val trackType: String = "",
    val isDefault: Boolean = false,
    val isForced: Boolean = false,
    val isHearingImpaired: Boolean = false
) : Parcelable

@Parcelize
data class AudioTrack(
    val id: Int = 0,
    val mediaId: Int = 0,
    val streamIndex: Int = 0,
    val language: String = "",
    val title: String? = null,
    val codecName: String = "",
    val channels: Int = 2,
    val sampleRate: Int = 44100,
    val bitrate: Int = 0,
    val trackType: String = "",
    val isDefault: Boolean = false
) : Parcelable
