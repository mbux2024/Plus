package com.homeflix.tv.domain.repository

import com.homeflix.tv.domain.model.MdbListRatings
import com.homeflix.tv.domain.model.RatingChip

/**
 * Repository for multi-source ratings from MDBList.
 */
interface RatingsRepository {

    /**
     * Get ratings for a media item from MDBList.
     * @param imdbId IMDb ID (preferred lookup)
     * @param tmdbId TMDB ID (fallback)
     */
    suspend fun getRatings(imdbId: String? = null, tmdbId: Int? = null): Result<MdbListRatings>

    /**
     * Convert raw ratings to display chips based on user's enabled providers.
     */
    suspend fun getRatingChips(imdbId: String? = null, tmdbId: Int? = null): Result<List<RatingChip>>
}
