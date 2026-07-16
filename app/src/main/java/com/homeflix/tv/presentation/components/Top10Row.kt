package com.homeflix.tv.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.homeflix.tv.presentation.theme.TextPrimary
import com.homeflix.tv.presentation.theme.Top10Stroke

/**
 * TOP 10 row (Plus look): huge outlined rank numbers peeking out from behind
 * each 2:3 poster. Ported from the Plus app's Top10Row, adapted to the
 * [MediaItem] display model. Signature mirrors [FocusExpandRow] for easy wiring.
 */
@Composable
fun Top10Row(
    sectionTitle: String,
    items: List<MediaItem>,
    onSelect: (MediaItem) -> Unit = {},
    firstItemFocusRequester: FocusRequester? = null
) {
    if (items.isEmpty()) return
    val top = items.take(10)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            ),
            modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(top, key = { _, it -> it.id }) { index, item ->
                Top10Card(
                    item = item,
                    rank = index + 1,
                    onClick = { onSelect(item) },
                    focusRequester = if (index == 0) firstItemFocusRequester else null
                )
            }
        }
    }
}

@Composable
private fun Top10Card(
    item: MediaItem,
    rank: Int,
    onClick: () -> Unit,
    focusRequester: FocusRequester?
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.07f else 1f, tween(180), label = "top10_scale")

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
            .onFocusChanged { focused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.key == Key.Enter || keyEvent.key == Key.DirectionCenter)
                ) {
                    onClick(); true
                } else false
            }
            .focusable()
            .clickable { onClick() }
    ) {
        // Giant outlined rank number, tucked behind the poster.
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 130.sp,
                fontWeight = FontWeight.Black,
                drawStyle = Stroke(width = 5f)
            ),
            color = Top10Stroke,
            modifier = Modifier.offset(x = 14.dp, y = 12.dp)
        )

        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .offset(x = (-18).dp)
                .width(118.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(6.dp))
                .then(
                    if (focused) Modifier.border(2.dp, Color.White, RoundedCornerShape(6.dp))
                    else Modifier
                )
        )
    }
}
