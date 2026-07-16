package com.homeflix.tv.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Multi-source ratings from MDBList API.
 * Displayed as a row of chips on the detail page.
 */
@Parcelize
data class MdbListRatings(
    val imdbRating: Float? = null,      // 0-10
    val imdbVotes: Int? = null,
    val rottenTomatoes: Int? = null,    // 0-100 (critics score)
    val audienceScore: Int? = null,     // 0-100 (RT audience)
    val metacritic: Int? = null,        // 0-100
    val tmdbRating: Float? = null,      // 0-10
    val traktRating: Float? = null,     // 0-10
    val letterboxdRating: Float? = null // 0-5
) : Parcelable

/**
 * Individual rating chip for display.
 */
@Parcelize
data class RatingChip(
    val source: RatingSource,
    val value: String,     // Formatted display value
    val rawValue: Float    // Numeric for sorting/comparison
) : Parcelable

enum class RatingSource(val displayName: String, val iconName: String) {
    IMDB("IMDb", "imdb"),
    ROTTEN_TOMATOES("RT", "rt"),
    AUDIENCE("Audience", "audience"),
    METACRITIC("Metacritic", "metacritic"),
    TMDB("TMDB", "tmdb"),
    TRAKT("Trakt", "trakt"),
    LETTERBOXD("Letterboxd", "letterboxd");
}
