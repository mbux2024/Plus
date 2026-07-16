package com.homeflix.tv.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.homeflix.tv.data.model.CatalogItem
import com.homeflix.tv.data.model.CatalogRow
import com.homeflix.tv.data.model.MediaType
import com.homeflix.tv.data.progress.WatchProgress
import com.homeflix.tv.data.tmdb.Genre
import com.homeflix.tv.data.tmdb.StreamingService
import com.homeflix.tv.ui.components.ContinueWatchingRow
import com.homeflix.tv.ui.components.FocusExpandRow
import com.homeflix.tv.ui.components.Top10Row
import com.homeflix.tv.ui.components.NAV_BAR_HEIGHT
import com.homeflix.tv.ui.components.toMediaItem
import com.homeflix.tv.ui.components.ShimmerLoadingSkeleton
import com.homeflix.tv.ui.components.TopNavBar
import com.homeflix.tv.ui.util.requestFocusAfterFrames

// ─────────────────────────────────────────────────────────────────────────────
// HOME SCREEN:
// Nav bar sits in its own row ABOVE the hero, on the app background
// (not overlaid). The hero is inset in a rounded rectangle below it, and
// content rows scroll beneath. "Your Next Watch" is the first row after the hero.
// ─────────────────────────────────────────────────────────────────────────────

private val PageBg = Color(0xFF141414)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSelect: (CatalogItem) -> Unit,
    onResume: (WatchProgress) -> Unit,
    onOpenService: (StreamingService) -> Unit,
    onOpenGenre: (Genre, String) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    Box(Modifier.fillMaxSize().background(PageBg)) {
        when {
            state.loading -> {
                Column(Modifier.fillMaxSize()) {
                    Spacer(Modifier.height(NAV_BAR_HEIGHT))
                    ShimmerLoadingSkeleton(rowCount = 4)
                }
            }
            state.error != null && state.homeRows.isEmpty() -> {
                Column(
                    Modifier.fillMaxSize().padding(48.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { viewModel.load() }, Modifier.padding(top = 16.dp)) { Text("Retry") }
                }
            }
            else -> {
                // Nav bar in its OWN row ABOVE the hero, on the app background
                // (no longer overlaid on the hero image).
                Column(Modifier.fillMaxSize()) {
                    TopNavBar(
                        selectedRoute = selectedTab.label,
                        onNavigate = { route ->
                            selectedTab = HomeTab.values().firstOrNull { it.label == route } ?: HomeTab.HOME
                        },
                        onSearch = onSearch,
                        onSettings = onSettings
                    )

                    // Content (inset hero + rows) scrolls below the nav bar.
                    BrowseContent(
                        tab = selectedTab,
                        state = state,
                        onSelect = onSelect,
                        onResume = onResume,
                        onHeroChanged = viewModel::loadHeroExtra
                    )
                }
            }
        }
    }
}

@Composable
private fun BrowseContent(
    tab: HomeTab,
    state: HomeUiState,
    onSelect: (CatalogItem) -> Unit,
    onResume: (WatchProgress) -> Unit,
    onHeroChanged: (CatalogItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows: List<CatalogRow> = when (tab) {
        HomeTab.TV_SHOWS -> state.showsRows
        HomeTab.MOVIES -> state.moviesRows
        HomeTab.MY_LIST -> buildList {
            if (state.myList.isNotEmpty()) add(CatalogRow("My List", state.myList))
            if (state.traktWatchlist.isNotEmpty()) add(CatalogRow("Trakt Watchlist", state.traktWatchlist))
        }
        else -> state.homeRows
    }

    // Hero pool: only items with backdrop images
    val heroPool = remember(rows) {
        rows.flatMap { it.items }
            .filter { it.backdropUrl != null }
            .distinctBy { "${it.type}_${it.id}" }
            .take(8)
    }

    // Top 10 ranking labels for hero (e.g. "#3 in TV Shows"), from ranked rows.
    val rankLabels = remember(rows) {
        buildMap {
            rows.filter { it.ranked }.forEach { row ->
                row.items.forEachIndexed { idx, item ->
                    val k = "${item.type}_${item.id}"
                    if (!containsKey(k)) {
                        val where = if (item.type == MediaType.TV) "TV Shows" else "Movies"
                        put(k, "#${idx + 1} in $where")
                    }
                }
            }
        }
    }

    val firstCardFocus = remember { FocusRequester() }
    var didAutoFocus by remember { mutableStateOf(false) }
    LaunchedEffect(heroPool.isNotEmpty()) {
        if (heroPool.isNotEmpty() && !didAutoFocus) {
            didAutoFocus = true
            firstCardFocus.requestFocusAfterFrames()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        // ── FULL-BLEED HERO (nav overlays on top of this) ────────────────
        if (heroPool.isNotEmpty()) {
            item(key = "hero") {
                CinematicHero(
                    items = heroPool,
                    heroExtras = state.heroExtras,
                    onPlay = onSelect,
                    onMoreInfo = onSelect,
                    onFeaturedChanged = onHeroChanged,
                    previewUrls = state.heroPreviews,
                    rankLabels = rankLabels,
                    playFocusRequester = firstCardFocus
                )
            }
        }

        // ── CONTINUE WATCHING / "Your Next Watch" (Home tab) ─────────────
        if (tab == HomeTab.HOME && state.continueWatching.isNotEmpty()) {
            item(key = "cw") {
                ContinueWatchingRow(
                    entries = state.continueWatching,
                    onResume = onResume,
                    onFocus = { _ -> },
                    onLongPress = { _ -> }
                )
            }
        }

        // ── RECOMMENDATIONS (Home tab) ───────────────────────────────────
        if (tab == HomeTab.HOME && state.recommendedRows.isNotEmpty()) {
            itemsIndexed(state.recommendedRows, key = { _, r -> "rec_${r.title}" }) { _, row ->
                FocusExpandRow(
                    sectionTitle = row.title,
                    items = row.items.map { it.toMediaItem(state.heroExtras["${it.type}_${it.id}"]) },
                    onSelect = { media ->
                        row.items.firstOrNull { "${it.type}_${it.id}" == media.id }?.let(onSelect)
                    }
                )
            }
        }

        // ── CONTENT ROWS (all tabs) ──────────────────────────────────────
        // Ranked rows render as a Top-10 row (giant outlined numbers behind
        // posters); everything else uses the standard poster row.
        itemsIndexed(rows, key = { _, r -> "${tab.label}_${r.title}" }) { _, row ->
            val mediaItems = row.items.map { it.toMediaItem(state.heroExtras["${it.type}_${it.id}"]) }
            val select: (com.homeflix.tv.ui.components.MediaItem) -> Unit = { media ->
                row.items.firstOrNull { "${it.type}_${it.id}" == media.id }?.let(onSelect)
            }
            if (row.ranked) {
                Top10Row(sectionTitle = row.title, items = mediaItems, onSelect = select)
            } else {
                FocusExpandRow(sectionTitle = row.title, items = mediaItems, onSelect = select)
            }
        }
    }
}
