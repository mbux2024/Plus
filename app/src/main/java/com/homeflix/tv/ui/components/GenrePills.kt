package com.homeflix.tv.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.homeflix.tv.data.tmdb.Genre

/**
 * Netflix-style horizontal genre filter pills.
 *
 * Rounded rectangular chips. The selected/focused chip gets a white
 * border + glow highlight. Tapping a chip opens that genre's browse screen.
 *
 * Curated to match the spec: Romance, Crime, Fantasy, Kids & Family,
 * Animation, Sci-Fi, Anime.
 */
private val PILL_GENRES = listOf(
    Genre("Romance", "", movieGenreId = 10749, tvGenreId = null),
    Genre("Crime", "", movieGenreId = 80, tvGenreId = 80),
    Genre("Fantasy", "", movieGenreId = 14, tvGenreId = 10765),
    Genre("Kids & Family", "", movieGenreId = 10751, tvGenreId = 10751),
    Genre("Animation", "", movieGenreId = 16, tvGenreId = 16),
    Genre("Sci-Fi", "", movieGenreId = 878, tvGenreId = 10765),
    Genre("Anime", "", movieGenreId = 16, tvGenreId = 16)
)

@Composable
fun GenrePills(
    onOpenGenre: (Genre, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        items(PILL_GENRES, key = { it.name }) { genre ->
            GenrePill(genre = genre, onClick = { onOpenGenre(genre, "all") })
        }
    }
}

@Composable
private fun GenrePill(genre: Genre, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Card(
        onClick = onClick,
        scale = CardDefaults.scale(focusedScale = 1.06f),
        shape = CardDefaults.shape(shape = RoundedCornerShape(10.dp)),
        colors = CardDefaults.colors(
            containerColor = Color(0x1FFFFFFF),
            focusedContainerColor = Color(0x33FFFFFF)
        ),
        border = CardDefaults.border(
            border = Border.None,
            // White glow/border highlight when focused (selected state).
            focusedBorder = Border(
                BorderStroke(2.dp, Color.White),
                shape = RoundedCornerShape(10.dp)
            )
        ),
        modifier = Modifier.onFocusChanged { focused = it.isFocused }
    ) {
        Text(
            genre.name,
            style = MaterialTheme.typography.titleSmall,
            color = if (focused) Color.White else Color(0xFFDDDDDD),
            fontWeight = if (focused) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
        )
    }
}
