package com.homeflix.tv.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ShimmerBase = Color(0xFF1A1A22)
private val ShimmerHighlight = Color(0xFF2E2E3A)

/**
 * Animated shimmer brush: a diagonal highlight sweeps across at a cinematic pace.
 */
@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    return Brush.linearGradient(
        colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
        start = Offset(translateAnim - 500f, translateAnim - 500f),
        end = Offset(translateAnim, translateAnim)
    )
}

/** Placeholder block for a single portrait poster card (2:3). */
@Composable
fun ShimmerPosterCard(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Box(
        modifier
            .width(130.dp)
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
    )
}

/** Placeholder block for a landscape hero card (16:9). */
@Composable
fun ShimmerHeroCard(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Box(
        modifier
            .fillMaxWidth(0.55f)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(14.dp))
            .background(brush)
    )
}

/** Shimmer placeholder for the hero section: 16:9 left + poster cards right. */
@Composable
fun ShimmerHeroSection(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Column(modifier.padding(horizontal = 48.dp, vertical = 12.dp)) {
        // Title placeholder
        Box(
            Modifier
                .width(200.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero tile placeholder
            Box(
                Modifier
                    .weight(0.55f)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(brush)
            )
            // Portrait cards placeholder column
            Row(
                Modifier.weight(0.45f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(3) {
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(brush)
                    )
                }
            }
        }
        // Metadata placeholder
        Box(
            Modifier
                .padding(top = 16.dp)
                .width(320.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(brush)
        )
        Box(
            Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.5f)
                .height(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(brush)
        )
    }
}

/** A full skeleton row: title placeholder + scrolling poster placeholders. */
@Composable
fun ShimmerRow(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Column(modifier.padding(vertical = 14.dp)) {
        // Title placeholder
        Box(
            Modifier
                .padding(start = 48.dp, bottom = 12.dp)
                .width(180.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
        // Card placeholders
        LazyRow(
            contentPadding = PaddingValues(start = 48.dp, end = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            userScrollEnabled = false
        ) {
            items(8) {
                ShimmerPosterCard()
            }
        }
    }
}

/** Multiple stacked shimmer rows for the full loading skeleton state. */
@Composable
fun ShimmerLoadingSkeleton(rowCount: Int = 4, modifier: Modifier = Modifier) {
    Column(modifier) {
        ShimmerHeroSection()
        repeat(rowCount) {
            ShimmerRow()
        }
    }
}

/**
 * Wraps content with a fade-in animation when [visible] transitions to true.
 * While not visible, shows the [placeholder] composable (typically a shimmer).
 * Use to smoothly transition from loading skeleton to real content.
 */
@Composable
fun FadeInContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = { ShimmerRow() },
    content: @Composable () -> Unit
) {
    Box(modifier) {
        androidx.compose.animation.AnimatedVisibility(
            visible = !visible,
            enter = androidx.compose.animation.fadeIn(
                animationSpec = androidx.compose.animation.core.tween(300)
            ),
            exit = androidx.compose.animation.fadeOut(
                animationSpec = androidx.compose.animation.core.tween(300)
            )
        ) {
            placeholder()
        }
        androidx.compose.animation.AnimatedVisibility(
            visible = visible,
            enter = androidx.compose.animation.fadeIn(
                animationSpec = androidx.compose.animation.core.tween(500, delayMillis = 100)
            ),
            exit = androidx.compose.animation.fadeOut(
                animationSpec = androidx.compose.animation.core.tween(200)
            )
        ) {
            content()
        }
    }
}
