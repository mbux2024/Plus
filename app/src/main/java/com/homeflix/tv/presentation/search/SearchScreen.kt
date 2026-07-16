package com.homeflix.tv.presentation.search

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.homeflix.tv.data.model.CatalogItem
import com.homeflix.tv.presentation.components.LoadingIndicator
import com.homeflix.tv.presentation.components.MediaCard
import com.homeflix.tv.presentation.theme.Background
import com.homeflix.tv.presentation.theme.PrimeBlue
import com.homeflix.tv.presentation.theme.Red
import com.homeflix.tv.presentation.theme.Surface
import com.homeflix.tv.presentation.theme.TextPrimary
import com.homeflix.tv.presentation.theme.TextSecondary
import com.homeflix.tv.presentation.util.requestFocusAfterFrames

// QWERTY on-screen keyboard (Plus layout).
private val KEYBOARD_ROWS = listOf(
    listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
    listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
    listOf("z", "x", "c", "v", "b", "n", "m"),
    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
)

private val CATEGORIES = listOf(
    "Action", "Comedy", "Drama", "Horror", "Sci-Fi", "Romance",
    "Thriller", "Animation", "Documentary", "Fantasy", "Crime", "Family"
)

/**
 * Search screen — Plus layout: a left panel with the query display, an on-screen
 * QWERTY keyboard and Categories, and a right panel showing recent searches
 * (empty query) or a results grid. MyBuild's voice + AI search + recent history
 * are preserved (mic button and AI submit remain wired to the ViewModel).
 */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onSelect: (CatalogItem) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (!spoken.isNullOrBlank()) viewModel.onVoiceQuery(spoken)
    }
    val startVoice: () -> Unit = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a title or describe what to watch")
        }
        runCatching { voiceLauncher.launch(intent) }
    }

    val keyboardFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { keyboardFocus.requestFocusAfterFrames() }

    Row(Modifier.fillMaxSize().background(Background)) {
        // ── LEFT PANEL: query display + keyboard + categories ────────────
        Column(
            modifier = Modifier
                .width(340.dp)
                .fillMaxHeight()
                .background(Color.Black.copy(alpha = 0.9f))
                .padding(16.dp)
        ) {
            // Query display + mic + clear
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, "Search", tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = state.query.ifEmpty { "Search..." },
                    color = if (state.query.isEmpty()) Color.Gray else Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                if (state.query.isNotEmpty()) {
                    IconKey(Icons.Default.Clear, "Clear") { viewModel.onQueryChange("") }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Virtual QWERTY keyboard
            KEYBOARD_ROWS.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    row.forEachIndexed { keyIndex, keyChar ->
                        VirtualKey(
                            label = keyChar.uppercase(),
                            modifier = Modifier.weight(1f),
                            focusRequester = if (rowIndex == 0 && keyIndex == 0) keyboardFocus else null,
                            onClick = { viewModel.onQueryChange(state.query + keyChar) }
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
            }
            // Space + backspace + voice
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                VirtualKey(
                    label = "SPACE",
                    modifier = Modifier.weight(2f),
                    onClick = { viewModel.onQueryChange(state.query + " ") }
                )
                VirtualKey(
                    label = null,
                    icon = Icons.Default.Backspace,
                    modifier = Modifier.weight(1f),
                    onClick = { if (state.query.isNotEmpty()) viewModel.onQueryChange(state.query.dropLast(1)) }
                )
                VirtualKey(
                    label = null,
                    icon = Icons.Default.Mic,
                    modifier = Modifier.weight(1f),
                    onClick = startVoice
                )
            }

            Spacer(Modifier.height(16.dp))

            // Categories
            Text(
                "Categories",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                itemsIndexed(CATEGORIES) { _, genre ->
                    CategoryItem(genre = genre, onClick = { viewModel.onQueryChange(genre) })
                }
            }
        }

        // ── RIGHT PANEL: recent (empty) or results grid ──────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            when {
                state.query.isBlank() -> RecentSearches(
                    recent = state.recent,
                    onSelect = viewModel::onRecentSelected,
                    onClear = viewModel::clearHistory
                )

                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator()
                }

                state.searched && state.results.isEmpty() -> Text(
                    if (state.aiActive)
                        "No AI matches for \u201c${state.query}\u201d. Try rephrasing, or check your Gemini key in Settings."
                    else
                        "No results for \u201c${state.query}\u201d.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.titleMedium
                )

                else -> {
                    Text(
                        text = state.header ?: "Search Results",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(136.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.results, key = { "${it.type}_${it.id}" }) { item ->
                            MediaCard(
                                item = item,
                                onClick = {
                                    viewModel.recordCurrentQuery()
                                    onSelect(item)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VirtualKey(
    label: String?,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.35f else 1f, tween(150), label = "key_scale")

    Box(
        modifier = modifier
            .height(32.dp)
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .background(if (focused) PrimeBlue else Color.Gray.copy(alpha = 0.28f))
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .onKeyEvent { e ->
                if (e.type == KeyEventType.KeyDown && (e.key == Key.Enter || e.key == Key.DirectionCenter)) {
                    onClick(); true
                } else false
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        } else {
            Text(label ?: "", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun IconKey(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (focused) PrimeBlue else Color.Transparent)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .onKeyEvent { e ->
                if (e.type == KeyEventType.KeyDown && (e.key == Key.Enter || e.key == Key.DirectionCenter)) {
                    onClick(); true
                } else false
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun CategoryItem(genre: String, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (focused) Red.copy(alpha = 0.85f) else Color.Transparent)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .onKeyEvent { e ->
                if (e.type == KeyEventType.KeyDown && (e.key == Key.Enter || e.key == Key.DirectionCenter)) {
                    onClick(); true
                } else false
            }
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            genre,
            color = if (focused) Color.White else Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            fontWeight = if (focused) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun RecentSearches(
    recent: List<String>,
    onSelect: (String) -> Unit,
    onClear: () -> Unit
) {
    Text(
        "Recent Searches",
        color = TextPrimary,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 20.dp)
    )
    if (recent.isEmpty()) {
        Text(
            "Type with the keyboard, tap the mic to speak, or pick a category. Your recent searches will show up here.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            RecentRow(term = "Clear history", accent = true, onClick = onClear)
            Spacer(Modifier.height(4.dp))
        }
        itemsIndexed(recent, key = { _, term -> term }) { _, term ->
            RecentRow(term = term, onClick = { onSelect(term) })
        }
    }
}

@Composable
private fun RecentRow(term: String, accent: Boolean = false, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    focused -> SurfaceVariantHighlight
                    accent -> Surface
                    else -> Color(0xFF1C1C20)
                }
            )
            .then(if (focused) Modifier.border(2.dp, Color.White, RoundedCornerShape(10.dp)) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .onKeyEvent { e ->
                if (e.type == KeyEventType.KeyDown && (e.key == Key.Enter || e.key == Key.DirectionCenter)) {
                    onClick(); true
                } else false
            }
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Text(term, color = Color.White, style = MaterialTheme.typography.bodyLarge)
    }
}

private val SurfaceVariantHighlight = Color(0xFF33333B)
