package com.homeflix.tv.presentation.components

import com.homeflix.tv.data.model.CatalogItem
import com.homeflix.tv.data.model.MediaType
import com.homeflix.tv.data.progress.WatchProgress
import com.homeflix.tv.data.tmdb.Genres

/**
 * Netflix 2025-style contextual tag logic.
 *
 * Returns a single short tag for a title based on deterministic rules.
 * Easy to swap in smarter ML-based logic later.
 *
 * Priority:
 * 1. If the item is in Continue Watching → "Continue Watching"
 * 2. If newly added (< 14 days based on year matching current year) → "New"
 * 3. If high rating (≥ 8.0) → "Popular"
 * 4. If matches genre of last-watched title → "Because you watched [X]"
 * 5. Fallback → first genre label (e.g. "Sci-Fi")
 */
fun getContextualTag(
    item: CatalogItem,
    continueWatching: List<WatchProgress> = emptyList(),
    lastWatched: CatalogItem? = null,
    isNew: Boolean = false
): String {
    // 1. In Continue Watching
    if (continueWatching.any { it.tmdbId == item.id && it.mediaType == item.type }) {
        return "Continue Watching"
    }

    // 2. Newly added
    if (isNew || isRecentRelease(item)) {
        return "New"
    }

    // 3. Popular (high rating)
    if (item.rating >= 8.0) {
        return "Popular"
    }

    // 4. Genre match with last watched
    lastWatched?.let { last ->
        val sharedGenre = item.genreIds.firstOrNull { it in last.genreIds }
        if (sharedGenre != null) {
            return "Because you watched ${last.title}"
        }
    }

    // 5. Fallback: first genre name
    val genreNames = Genres.namesFor(item.genreIds, max = 1)
    return genreNames.firstOrNull() ?: if (item.type == MediaType.TV) "Series" else "Film"
}

/** Heuristic: if the item's year matches the current year, treat as "new". */
private fun isRecentRelease(item: CatalogItem): Boolean {
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()
    return item.year == currentYear
}
