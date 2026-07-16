package com.homeflix.tv.ui.home

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.homeflix.tv.data.model.CatalogItem
import com.homeflix.tv.data.tmdb.Genres
import com.homeflix.tv.ui.theme.Background
import com.homeflix.tv.ui.theme.PrimeBlue
import com.homeflix.tv.ui.theme.Red
import com.homeflix.tv.ui.theme.Surface
import com.homeflix.tv.ui.theme.SurfaceVariant
import com.homeflix.tv.ui.theme.TextPrimary
import com.homeflix.tv.ui.theme.TextSecondary
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
// CINEMATIC HERO — ported verbatim (visually) from mbux2024/Plus
// (presentation/components/CinematicHero.kt), adapted to Streambert's
// CatalogItem/HeroExtra model, tv.material3 theme tokens, and the existing
// YouTube preview-clip pipeline (state.heroPreviews).
//
// Prime Video layout with Netflix slide behavior: full-bleed backdrop that
// auto-plays a muted preview clip after a short dwell, strong left gradient +
// bottom fade into the page background, metadata badges, focusable Play /
// More Info actions, and bottom-center slide dots.
//
// Backdrop restored from the feature/cinematic-home-ui branch: full-bleed
// 500dp height with the preview video filling via RESIZE_MODE_ZOOM (crop to
// fill), not an inset/rounded 16:9 card.
//
// Exact visuals preserved from Plus: 500dp height, H-gradient stops with
// endX=1400f, V-gradient with startY=750f, content padding start=56/bottom=88,
// slide-dot sizing, and the HeroActionButton styling.
// ─────────────────────────────────────────────────────────────────────────────

// Ported hex values from Plus's theme (no matching Streambert tokens):
//   RatingGold  -> reuse the IMDb gold already used elsewhere in the app.
//   BadgeOutline-> exact Plus hex, kept for the certification badge border.
private val RatingGold = Color(0xFFF5C518)
private val BadgeOutline = Color(0xFF3A4750)

@Composable
fun CinematicHero(
    items: List<CatalogItem>,
    heroExtras: Map<String, HeroExtra>,
    onPlay: (CatalogItem) -> Unit,
    onMoreInfo: (CatalogItem) -> Unit,
    onFeaturedChanged: (CatalogItem) -> Unit,
    modifier: Modifier = Modifier,
    previewUrls: Map<String, String> = emptyMap(),
    @Suppress("UNUSED_PARAMETER") rankLabels: Map<String, String> = emptyMap(),
    playFocusRequester: FocusRequester? = null
) {
    if (items.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }
    val safeIndex = currentIndex.coerceIn(items.indices)
    val current = items[safeIndex]
    val key = "${current.type}_${current.id}"
    val extra = heroExtras[key]

    // Auto-advance (longer dwell so the preview video gets screen time)
    LaunchedEffect(safeIndex, items.size) {
        if (items.size > 1) {
            delay(16_000)
            currentIndex = (safeIndex + 1) % items.size
        }
    }

    // Notify parent so it can lazily load hero extras + resolve the preview clip.
    LaunchedEffect(safeIndex) {
        onFeaturedChanged(items[safeIndex])
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            // Full-bleed backdrop (restored from the old branch): fixed 500dp
            // height, edge-to-edge, no rounded inset.
            .height(500.dp)
    ) {
        // Backdrop + delayed muted preview clip (crossfade between slides)
        Crossfade(
            targetState = safeIndex,
            animationSpec = tween(durationMillis = 900),
            label = "hero_bg"
        ) { idx ->
            val media = items.getOrElse(idx) { current }
            val mediaKey = "${media.type}_${media.id}"
            Box(Modifier.fillMaxSize()) {
                HeroBackgroundVideo(
                    backdropUrl = media.backdropUrl ?: media.posterUrl,
                    videoUrl = previewUrls[mediaKey],
                    startDelayMs = 3000,
                    playbackEnabled = idx == safeIndex,
                    contentDescription = media.title
                )
            }
        }

        // Prime-style gradient: strong left panel + fade to page bg at bottom
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Background.copy(alpha = 0.94f),
                            Background.copy(alpha = 0.55f),
                            Color.Transparent
                        ),
                        endX = 1400f
                    )
                )
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Surface),
                        startY = 750f
                    )
                )
        )

        // Content column (left-aligned like Prime)
        val contentVisible = remember { mutableStateOf(false) }
        LaunchedEffect(current.id) {
            contentVisible.value = false
            delay(250)
            contentVisible.value = true
        }
        val alpha by animateFloatAsState(
            targetValue = if (contentVisible.value) 1f else 0f,
            animationSpec = tween(700), label = "hero_content"
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 56.dp, bottom = 88.dp, end = 500.dp)
                .graphicsLayer { this.alpha = alpha },
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Brand strip (Prime shows "prime" above the logo)
            Text(
                text = "STREAMBERT",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Red,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
            )

            // No title-logo art API in our codebase -> text fallback (Plus's fallback path).
            Text(
                text = current.title,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Prime-style teal accent line ("New Season" equivalent)
            Text(
                text = "Newly Added",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color(0xFF4FD8CE),
                    fontWeight = FontWeight.SemiBold
                )
            )

            // Metadata row: ★ rating · year · runtime/seasons · genres · cert
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val rating = extra?.imdbRating ?: current.rating.takeIf { it > 0.0 }
                if (rating != null && rating > 0.0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("★", color = RatingGold, style = MaterialTheme.typography.titleSmall)
                        Text(
                            String.format("%.1f", rating),
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
                current.year?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = TextSecondary, style = MaterialTheme.typography.titleSmall)
                }
                // Our HeroExtra carries a pre-formatted label (movie runtime or TV seasons).
                (extra?.runtimeLabel ?: extra?.episodesLabel)?.let {
                    Text(it, color = TextSecondary, style = MaterialTheme.typography.titleSmall)
                }
                val genreNames = Genres.namesFor(current.genreIds)
                if (genreNames.isNotEmpty()) {
                    Text(
                        genreNames.joinToString(" • "),
                        color = TextSecondary,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                CertBadge(extra?.contentRating ?: "PG-13")
                // NOTE: Plus rendered a QualityBadge here; Streambert has no
                // catalog-level quality field (quality is only known per-stream),
                // so the quality badge is intentionally dropped.
            }

            // Description
            current.overview?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextPrimary.copy(alpha = 0.92f),
                        lineHeight = 24.sp
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Actions: Play (white) + More Info (Prime-blue focus -> brand red)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                HeroActionButton(
                    label = "Play",
                    icon = { fg -> Icon(Icons.Default.PlayArrow, null, Modifier.size(26.dp), tint = fg) },
                    primary = true,
                    focusRequester = playFocusRequester,
                    onClick = { onPlay(current) },
                    onNavigateLeft = {
                        if (items.size > 1) currentIndex = if (safeIndex > 0) safeIndex - 1 else items.size - 1
                    },
                    onNavigateRight = null
                )
                HeroActionButton(
                    label = "More Info",
                    icon = { fg -> Icon(Icons.Default.Info, null, Modifier.size(20.dp), tint = fg) },
                    primary = false,
                    onClick = { onMoreInfo(current) },
                    onNavigateLeft = null,
                    onNavigateRight = {
                        if (items.size > 1) currentIndex = (safeIndex + 1) % items.size
                    }
                )
            }
        }

        // Slide dots (bottom-center like Prime)
        if (items.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEachIndexed { i, _ ->
                    Box(
                        Modifier
                            .size(width = if (i == safeIndex) 22.dp else 7.dp, height = 7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (i == safeIndex) TextPrimary else TextPrimary.copy(alpha = 0.35f))
                    )
                }
            }
        }
    }
}

@Composable
private fun CertBadge(text: String) {
    Box(
        Modifier
            .border(1.dp, BadgeOutline, RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text,
            color = TextSecondary,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

/**
 * Prime-style pill button: white/translucent at rest, brand-red when focused
 * (Plus used its "PrimeBlue" accent here; mapped to Streambert's brand Red).
 */
@Composable
private fun HeroActionButton(
    label: String,
    icon: @Composable (fg: Color) -> Unit,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    onNavigateDown: (() -> Unit)? = null,
    onNavigateLeft: (() -> Unit)? = null,
    onNavigateRight: (() -> Unit)? = null
) {
    var focused by remember { mutableStateOf(false) }
    val bg = when {
        focused -> if (primary) Color.White else PrimeBlue
        primary -> Color.White.copy(alpha = 0.9f)
        else -> SurfaceVariant.copy(alpha = 0.85f)
    }
    val fg = when {
        focused -> if (primary) Color.Black else Color.White
        primary -> Color.Black
        else -> TextPrimary
    }
    val scale by animateFloatAsState(if (focused) 1.06f else 1f, tween(150), label = "btn_scale")

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.Enter, Key.DirectionCenter -> {
                            onClick(); true
                        }
                        Key.DirectionDown -> onNavigateDown?.invoke() != null
                        Key.DirectionLeft -> {
                            if (onNavigateLeft != null) {
                                onNavigateLeft(); true
                            } else false
                        }
                        Key.DirectionRight -> {
                            if (onNavigateRight != null) {
                                onNavigateRight(); true
                            } else false
                        }
                        Key.DirectionUp -> true // consume - nothing above hero
                        else -> false
                    }
                } else false
            }
            .focusable()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            icon(fg)
            Text(
                label,
                color = fg,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * Prime/Netflix-style ambient background video (ported from Plus's
 * BackgroundVideo). Shows [backdropUrl] instantly, then after [startDelayMs]
 * starts a muted, looping preview clip and crossfades it in once the first
 * frame renders. Lifecycle-safe: pauses/releases with the host lifecycle and
 * on dispose; falls back to the static backdrop when no preview URL exists.
 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun HeroBackgroundVideo(
    backdropUrl: String?,
    videoUrl: String?,
    modifier: Modifier = Modifier,
    startDelayMs: Long = 2500,
    playbackEnabled: Boolean = true,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var firstFrameRendered by remember { mutableStateOf(false) }

    val videoAlpha by animateFloatAsState(
        targetValue = if (firstFrameRendered) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "bg_video_alpha"
    )

    // (Re)start playback when the target video changes.
    LaunchedEffect(videoUrl, playbackEnabled) {
        firstFrameRendered = false
        player?.release()
        player = null
        if (videoUrl.isNullOrBlank() || !playbackEnabled) return@LaunchedEffect

        delay(startDelayMs)

        val exo = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    firstFrameRendered = true
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    // Preview unavailable - stay on the backdrop image.
                    firstFrameRendered = false
                }
            })
            prepare()
        }
        player = exo
    }

    // Hard lifecycle guarantees: never play while not visible.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> player?.pause()
                Lifecycle.Event.ON_STOP -> {
                    player?.release()
                    player = null
                    firstFrameRendered = false
                }
                Lifecycle.Event.ON_RESUME -> player?.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player?.release()
            player = null
        }
    }

    // Backdrop image - always present underneath.
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(backdropUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )

    // Video surface, faded in over the backdrop.
    if (player != null) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    // ZOOM (restored from the old branch): the preview clip fills
                    // the full-bleed backdrop, cropping to fill rather than
                    // letterboxing.
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                }
            },
            update = { view -> view.player = player },
            modifier = modifier
                .fillMaxSize()
                .graphicsLayer { alpha = videoAlpha }
        )
    }
}
