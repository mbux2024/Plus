package com.homeflix.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.homeflix.tv.data.model.CatalogItem
import com.homeflix.tv.data.model.MediaType
import com.homeflix.tv.data.tmdb.Genres
import com.homeflix.tv.ui.home.HeroExtra
import com.homeflix.tv.ui.theme.RatingGold
import com.homeflix.tv.ui.theme.TextPrimary
import com.homeflix.tv.ui.theme.TextSecondary

/**
 * Hero height helper for TV (retained for compatibility). A flat 340.dp was
 * phone-tuned; on a TV the full width is much larger, so scale off screen width.
 */
object HeroSizing {
    fun heightFor(screenWidthDp: Dp): Dp = (screenWidthDp.value * 0.65f).dp
}


/** UI model for a row card + its metadata. */
data class MediaItem(
    val id: String,
    val title: String,
    val imageUrl: String,
    val rating: String,
    val genre: String,
    val year: String,
    val episodes: String? = null,
    val maturity: String,
    val description: String
)

/**
 * Maps a real [CatalogItem] (+ optional lazily-loaded [HeroExtra]) into the
 * [MediaItem] display model consumed by [FocusExpandRow].
 */
fun CatalogItem.toMediaItem(extra: HeroExtra?): MediaItem = MediaItem(
    id = "${type}_${id}",
    title = title,
    imageUrl = posterUrl ?: backdropUrl ?: "",
    rating = extra?.imdbRating?.let { String.format(java.util.Locale.US, "%.1f", it) }
        ?: rating.takeIf { it > 0.0 }?.let { String.format(java.util.Locale.US, "%.1f", it) }
        ?: "",
    genre = Genres.namesFor(genreIds, max = 2).joinToString(", "),
    year = year ?: "",
    episodes = if (type == MediaType.TV) extra?.episodesLabel else extra?.runtimeLabel,
    maturity = extra?.contentRating ?: "",
    description = overview ?: ""
)


/**
 * Netflix/Prime-style content row (Plus look): a section title above a LazyRow
 * of fixed 2:3 portrait posters. Each poster scales up with a white border on
 * focus and reveals a dark overlay with its title / year / rating on the card
 * itself — no separate metadata panel below the row.
 */
@Composable
fun FocusExpandRow(
    sectionTitle: String,
    items: List<MediaItem>,
    onSelect: (MediaItem) -> Unit = {},
    firstItemFocusRequester: FocusRequester? = null
) {
    if (items.isEmpty()) return

    Column {
        Text(
            text = sectionTitle,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 24.dp)
        )
        Spacer(Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                PosterCard(
                    item = item,
                    focusRequester = if (index == 0) firstItemFocusRequester else null,
                    onClick = { onSelect(item) }
                )
            }
        }
    }
}


/**
 * Fixed 2:3 portrait poster (Plus NetflixMediaCard style): 1.1x focus scale,
 * white 2dp focus border, raised z-index, and an on-card info overlay on focus.
 */
@Composable
private fun PosterCard(
    item: MediaItem,
    focusRequester: FocusRequester?,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1f,
        animationSpec = tween(200),
        label = "poster_scale"
    )

    Box(
        modifier = Modifier
            .width(120.dp)
            .aspectRatio(2f / 3f)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .zIndex(if (isFocused) 10f else 1f)
            .clip(shape)
            .then(if (isFocused) Modifier.border(2.dp, Color.White, shape) else Modifier)
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Info overlay — only while focused (Plus behavior).
        if (isFocused) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.45f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.85f)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = item.title,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.year.isNotBlank()) {
                            Text(item.year, color = TextSecondary, fontSize = 10.sp)
                        }
                        if (item.rating.isNotBlank()) {
                            Text("★ ${item.rating}", color = RatingGold, fontSize = 10.sp)
                        }
                        if (item.maturity.isNotBlank()) {
                            Text(item.maturity, color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
