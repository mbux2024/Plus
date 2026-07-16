package com.homeflix.tv.presentation.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.homeflix.tv.domain.model.*
import com.homeflix.tv.presentation.navigation.Screen

private val NetflixRed = Color(0xFFE50914)

@Composable
fun DetailsScreen(
    navController: NavHostController,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val streamState by viewModel.streamState.collectAsState()
    val isInMyList by viewModel.isInMyList.collectAsState()

    // Navigate to player when stream is ready
    LaunchedEffect(streamState) {
        when (val state = streamState) {
            is StreamState.Ready -> {
                val detail = (uiState as? DetailsUiState.Success)?.detail
                navController.navigate(
                    Screen.VideoPlayer.createRoute(
                        streamUrl = state.url,
                        title = detail?.media?.title ?: "",
                        tmdbId = detail?.media?.id ?: 0
                    )
                )
                viewModel.resetStreamState()
            }
            is StreamState.TrailerReady -> {
                navController.navigate(
                    Screen.VideoPlayer.createRoute(
                        streamUrl = state.url,
                        title = "Trailer: ${state.title}"
                    )
                )
                viewModel.resetStreamState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
    ) {
        when (val state = uiState) {
            is DetailsUiState.Loading -> {
                CircularProgressIndicator(
                    color = NetflixRed,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is DetailsUiState.Error -> {
                Text(
                    text = state.message,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
            }
            is DetailsUiState.Success -> {
                DetailContent(
                    detail = state.detail,
                    streamState = streamState,
                    isInMyList = isInMyList,
                    onPlay = { viewModel.playBest() },
                    onTrailer = { youtubeId -> viewModel.playTrailer(youtubeId) },
                    onToggleMyList = { viewModel.toggleMyList() },
                    onLoadSources = { viewModel.loadSources() }
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    detail: TmdbMediaDetail,
    streamState: StreamState,
    isInMyList: Boolean,
    onPlay: () -> Unit,
    onTrailer: (String) -> Unit,
    onToggleMyList: () -> Unit,
    onLoadSources: () -> Unit
) {
    val media = detail.media

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Backdrop with gradient
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                AsyncImage(
                    model = media.backdropUrl("w1280"),
                    contentDescription = media.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF141414)),
                                startY = 200f
                            )
                        )
                )
            }
        }

        // Title + Meta
        item {
            Column(modifier = Modifier.padding(horizontal = 48.dp)) {
                Text(
                    text = media.title,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Meta row: year • rating • runtime • certification
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    media.year?.let {
                        Text("$it", color = Color(0xFFB3B3B3), fontSize = 14.sp)
                    }
                    if (media.voteAverage > 0) {
                        Text(
                            text = "★ ${String.format("%.1f", media.voteAverage)}",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    media.runtime?.let {
                        Text("${it}min", color = Color(0xFFB3B3B3), fontSize = 14.sp)
                    }
                    media.certification?.let {
                        Text(
                            text = it,
                            color = Color(0xFFB3B3B3),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(Color(0xFF333333), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Genres
                if (media.genres.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = media.genres.joinToString(" • ") { it.name },
                        color = Color(0xFF999999),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Action buttons
        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 48.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play button
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    enabled = streamState !is StreamState.Resolving,
                    modifier = Modifier.height(48.dp)
                ) {
                    if (streamState is StreamState.Resolving) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.PlayArrow, "Play", tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                // Sources picker
                IconButton(onClick = onLoadSources) {
                    Icon(Icons.Default.List, "Sources", tint = Color.White)
                }

                // Trailer button
                detail.trailer?.let { trailer ->
                    Button(
                        onClick = { onTrailer(trailer.key) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                    ) {
                        Icon(Icons.Default.PlayArrow, "Trailer", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trailer", color = Color.White)
                    }
                }

                // My List toggle
                IconButton(onClick = onToggleMyList) {
                    Icon(
                        imageVector = if (isInMyList) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = if (isInMyList) "Remove from My List" else "Add to My List",
                        tint = Color.White
                    )
                }
            }
        }

        // Overview
        media.overview?.let { overview ->
            item {
                Text(
                    text = overview,
                    color = Color(0xFFCCCCCC),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 48.dp)
                )
            }
        }

        // Cast row
        if (detail.credits.cast.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Cast",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(detail.credits.cast.take(15)) { member ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(80.dp)
                        ) {
                            AsyncImage(
                                model = member.profileUrl(),
                                contentDescription = member.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF333333))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                member.name,
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            member.character?.let {
                                Text(
                                    it,
                                    color = Color(0xFF999999),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Similar titles
        if (detail.similar.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "More Like This",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(detail.similar) { item ->
                        AsyncImage(
                            model = item.posterUrl("w185"),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF333333))
                        )
                    }
                }
            }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }

    // Stream error snackbar
    if (streamState is StreamState.Error) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = (streamState as StreamState.Error).message,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .background(Color(0xCC333333), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            )
        }
    }
}
