package com.homeflix.tv.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.homeflix.tv.domain.model.*

private val NetflixRed = Color(0xFFE50914)
private val DarkBackground = Color(0xFF141414)
private val CardBackground = Color(0xFF1F1F1F)
private val TextPrimary = Color.White
private val TextSecondary = Color(0xFFB3B3B3)

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val addons by viewModel.installedAddons.collectAsState()
    val torboxStatus by viewModel.torboxStatus.collectAsState()
    val rdStatus by viewModel.rdStatus.collectAsState()

    var selectedCategory by remember { mutableStateOf(SettingsCategory.PLAYBACK) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Left rail — category navigation
        SettingsCategoryRail(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        // Right content — settings for selected category
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            when (selectedCategory) {
                SettingsCategory.PLAYBACK -> PlaybackSettings(settings, viewModel)
                SettingsCategory.CONTENT_DISCOVERY -> ContentDiscoverySettings(settings, viewModel)
                SettingsCategory.INTEGRATION -> IntegrationSettings(settings, viewModel, torboxStatus, rdStatus)
                SettingsCategory.ADDONS -> AddonsSettings(settings, addons, viewModel)
                SettingsCategory.ABOUT -> AboutSettings()
            }
        }
    }
}

@Composable
private fun SettingsCategoryRail(
    selectedCategory: SettingsCategory,
    onCategorySelected: (SettingsCategory) -> Unit
) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(Color(0xFF0A0A0A))
            .padding(vertical = 24.dp)
    ) {
        Text(
            text = "Settings",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsCategory.entries.forEach { category ->
            val isSelected = category == selectedCategory
            var isFocused by remember { mutableStateOf(false) }

            val icon = when (category) {
                SettingsCategory.PLAYBACK -> Icons.Default.PlayArrow
                SettingsCategory.CONTENT_DISCOVERY -> Icons.Default.Search
                SettingsCategory.INTEGRATION -> Icons.Default.Link
                SettingsCategory.ADDONS -> Icons.Default.Extension
                SettingsCategory.ABOUT -> Icons.Default.Info
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused }
                    .background(
                        when {
                            isSelected -> NetflixRed.copy(alpha = 0.2f)
                            isFocused -> Color.White.copy(alpha = 0.1f)
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .then(
                        if (isSelected) Modifier.border(1.dp, NetflixRed, RoundedCornerShape(8.dp))
                        else if (isFocused) Modifier.border(1.dp, Color.White, RoundedCornerShape(8.dp))
                        else Modifier
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = category.label,
                    tint = if (isSelected) NetflixRed else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = category.label,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

// ─── Playback ────────────────────────────────────────────────────────────────

@Composable
private fun PlaybackSettings(settings: AppSettings, viewModel: SettingsViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("Video Player Engine") }
        item {
            SettingsChoiceRow(
                label = "Engine",
                currentValue = settings.playerEngine.label,
                options = PlayerEngine.entries.map { it.label },
                onSelected = { idx -> viewModel.setPlayerEngine(PlayerEngine.entries[idx]) }
            )
        }
        item {
            SettingsToggleRow(
                label = "Hardware Decoding",
                description = "Force software decode if device shows garbled video",
                isChecked = settings.hardwareDecoding,
                onToggle = { viewModel.setHardwareDecoding(it) }
            )
        }

        item { SectionHeader("Quality") }
        item {
            SettingsChoiceRow(
                label = "Preferred Quality",
                currentValue = settings.preferredQuality.label,
                options = PreferredQuality.entries.map { it.label },
                onSelected = { idx -> viewModel.setPreferredQuality(PreferredQuality.entries[idx]) }
            )
        }

        item { SectionHeader("Language") }
        item {
            SettingsTextRow(
                label = "Preferred Audio Language",
                currentValue = settings.preferredAudioLanguage,
                onValueChange = { viewModel.setPreferredAudioLanguage(it) }
            )
        }
        item {
            SettingsTextRow(
                label = "Preferred Subtitle Language",
                currentValue = settings.preferredSubtitleLanguage,
                onValueChange = { viewModel.setPreferredSubtitleLanguage(it) }
            )
        }
        item {
            SettingsChoiceRow(
                label = "Subtitle Size",
                currentValue = settings.subtitleSize.label,
                options = SubtitleSize.entries.map { it.label },
                onSelected = { idx -> viewModel.setSubtitleSize(SubtitleSize.entries[idx]) }
            )
        }

        item { SectionHeader("Behavior") }
        item {
            SettingsToggleRow(
                label = "Auto-play Next Episode",
                description = "Countdown appears near end of episode, rolls into next",
                isChecked = settings.autoplayNextEpisode,
                onToggle = { viewModel.setAutoplayNext(it) }
            )
        }
        item {
            SettingsToggleRow(
                label = "Skip Intro",
                description = "Netflix-style Skip Intro button in early episode",
                isChecked = settings.skipIntroEnabled,
                onToggle = { viewModel.setSkipIntro(it) }
            )
        }
    }
}

// ─── Content Discovery ───────────────────────────────────────────────────────

@Composable
private fun ContentDiscoverySettings(settings: AppSettings, viewModel: SettingsViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("TMDB API") }
        item {
            SettingsTextRow(
                label = "TMDB API Key (Read Access Token)",
                currentValue = if (settings.tmdbApiKey.isNotBlank()) "••••••••${settings.tmdbApiKey.takeLast(8)}" else "Not set",
                onValueChange = { viewModel.setTmdbApiKey(it) },
                isSecret = true
            )
        }

        item { SectionHeader("TMDB Enrichment") }
        item {
            SettingsToggleRow("Artwork", "Posters, backdrops, logos", settings.tmdbEnrichArtwork) {
                viewModel.setTmdbEnrichment("artwork", it)
            }
        }
        item {
            SettingsToggleRow("Basic Info", "Title, year, rating, overview", settings.tmdbEnrichBasicInfo) {
                viewModel.setTmdbEnrichment("basic", it)
            }
        }
        item {
            SettingsToggleRow("Details", "Runtime, status, tagline", settings.tmdbEnrichDetails) {
                viewModel.setTmdbEnrichment("details", it)
            }
        }
        item {
            SettingsToggleRow("Cast & Crew", "Directors, actors", settings.tmdbEnrichCast) {
                viewModel.setTmdbEnrichment("cast", it)
            }
        }
        item {
            SettingsToggleRow("Trailers", "YouTube trailer playback", settings.tmdbEnrichTrailers) {
                viewModel.setTmdbEnrichment("trailers", it)
            }
        }
        item {
            SettingsToggleRow("More Like This", "Similar titles", settings.tmdbEnrichMoreLikeThis) {
                viewModel.setTmdbEnrichment("more_like_this", it)
            }
        }
        item {
            SettingsToggleRow("Collections", "Movie collections", settings.tmdbEnrichCollections) {
                viewModel.setTmdbEnrichment("collections", it)
            }
        }
    }
}

// ─── Integration ─────────────────────────────────────────────────────────────

@Composable
private fun IntegrationSettings(
    settings: AppSettings,
    viewModel: SettingsViewModel,
    torboxStatus: DebridConnectionStatus,
    rdStatus: DebridConnectionStatus
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("Debrid Services") }
        item {
            DebridServiceRow(
                serviceName = "TorBox",
                status = torboxStatus,
                onConnect = { key -> viewModel.connectTorBox(key) },
                onDisconnect = { viewModel.disconnectTorBox() }
            )
        }
        item {
            DebridServiceRow(
                serviceName = "Real-Debrid",
                status = rdStatus,
                onConnect = { key -> viewModel.connectRealDebrid(key) },
                onDisconnect = { viewModel.disconnectRealDebrid() }
            )
        }

        item { SectionHeader("MDBList (Ratings)") }
        item {
            SettingsTextRow(
                label = "MDBList API Key",
                currentValue = if (settings.mdbListApiKey.isNotBlank()) "Connected" else "Not set",
                onValueChange = { viewModel.setMdbListApiKey(it) },
                isSecret = true
            )
        }

        item { SectionHeader("Rating Providers") }
        item { SettingsToggleRow("IMDb", null, settings.showImdbRating) { viewModel.setRatingToggle("imdb", it) } }
        item { SettingsToggleRow("Rotten Tomatoes", null, settings.showRottenTomatoes) { viewModel.setRatingToggle("rt", it) } }
        item { SettingsToggleRow("Audience Score", null, settings.showAudienceScore) { viewModel.setRatingToggle("audience", it) } }
        item { SettingsToggleRow("Metacritic", null, settings.showMetacritic) { viewModel.setRatingToggle("metacritic", it) } }
        item { SettingsToggleRow("TMDB", null, settings.showTmdbRating) { viewModel.setRatingToggle("tmdb", it) } }
        item { SettingsToggleRow("Trakt", null, settings.showTraktRating) { viewModel.setRatingToggle("trakt", it) } }
        item { SettingsToggleRow("Letterboxd", null, settings.showLetterboxdRating) { viewModel.setRatingToggle("letterboxd", it) } }

        item { SectionHeader("OMDb") }
        item {
            SettingsTextRow(
                label = "OMDb API Key",
                currentValue = if (settings.omdbApiKey.isNotBlank()) "Connected" else "Not set",
                onValueChange = { viewModel.setOmdbApiKey(it) },
                isSecret = true
            )
        }
    }
}

// ─── Add-ons ─────────────────────────────────────────────────────────────────

@Composable
private fun AddonsSettings(
    settings: AppSettings,
    addons: List<StremioAddon>,
    viewModel: SettingsViewModel
) {
    var addonUrlInput by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("Stream Source") }
        item {
            SettingsChoiceRow(
                label = "Source Mode",
                currentValue = settings.streamSourceMode.label,
                options = StreamSourceMode.entries.map { it.label },
                onSelected = { idx -> viewModel.setStreamSourceMode(StreamSourceMode.entries[idx]) }
            )
        }

        if (settings.streamSourceMode == StreamSourceMode.CUSTOM_ADDON) {
            item {
                SettingsTextRow(
                    label = "Custom Addon URL (Comet)",
                    currentValue = settings.customAddonUrl.ifBlank { "Not set" },
                    onValueChange = { viewModel.setCustomAddonUrl(it) }
                )
            }
        }

        item { SectionHeader("Installed Add-ons") }
        items(addons.sortedBy { it.priority }) { addon ->
            AddonRow(
                addon = addon,
                onToggle = { viewModel.toggleAddon(addon.id, it) },
                onRemove = { viewModel.removeAddon(addon.id) },
                onRefresh = { viewModel.refreshAddon(addon.id) }
            )
        }

        item { SectionHeader("Install New Add-on") }
        item {
            SettingsTextRow(
                label = "Manifest URL",
                currentValue = addonUrlInput.ifBlank { "Paste addon URL..." },
                onValueChange = { addonUrlInput = it }
            )
        }
        item {
            if (addonUrlInput.isNotBlank()) {
                Text(
                    text = "Press Enter to install",
                    color = NetflixRed,
                    fontSize = 13.sp
                )
            }
        }

        item { SectionHeader("Subtitles") }
        item {
            SettingsTextRow(
                label = "Subtitles Add-on URL",
                currentValue = settings.subtitleAddonUrl,
                onValueChange = { viewModel.installAddon(it) }
            )
        }

        item { SectionHeader("Manage on Phone") }
        item {
            Text(
                text = "Tap to start a web server on this device. Scan the QR code from your phone to add/remove add-ons from a browser.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

// ─── About ───────────────────────────────────────────────────────────────────

@Composable
private fun AboutSettings() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("HomeFlixTV")
        Text("Version 1.0.0", color = TextSecondary, fontSize = 14.sp)
        Text(
            "TMDB-powered catalog • Stremio addon streams • TorBox + Real-Debrid",
            color = TextSecondary,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("MPV Player Engine (libmpv)", color = TextSecondary, fontSize = 13.sp)
        Text("Jetpack Compose for TV", color = TextSecondary, fontSize = 13.sp)
        Text("Netflix-style UI with D-pad navigation", color = TextSecondary, fontSize = 13.sp)
    }
}

// ─── Reusable Components ─────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    label: String,
    description: String?,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextPrimary, fontSize = 15.sp)
            if (description != null) {
                Text(description, color = TextSecondary, fontSize = 12.sp)
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = NetflixRed)
        )
    }
}

@Composable
private fun SettingsChoiceRow(
    label: String,
    currentValue: String,
    options: List<String>,
    onSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontSize = 15.sp)
        Text(currentValue, color = NetflixRed, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SettingsTextRow(
    label: String,
    currentValue: String,
    onValueChange: (String) -> Unit,
    isSecret: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Text(
            text = currentValue,
            color = TextSecondary,
            fontSize = 13.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun DebridServiceRow(
    serviceName: String,
    status: DebridConnectionStatus,
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit
) {
    val statusText = when (status) {
        is DebridConnectionStatus.NotConnected -> "Tap to connect"
        is DebridConnectionStatus.Connecting -> "Connecting..."
        is DebridConnectionStatus.Connected -> "Connected"
        is DebridConnectionStatus.Error -> "Error: ${status.message}"
    }
    val statusColor = when (status) {
        is DebridConnectionStatus.Connected -> Color(0xFF4CAF50)
        is DebridConnectionStatus.Error -> Color(0xFFF44336)
        else -> TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(serviceName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(statusText, color = statusColor, fontSize = 12.sp)
        }
        if (status is DebridConnectionStatus.Connected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Connected",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun AddonRow(
    addon: StremioAddon,
    onToggle: (Boolean) -> Unit,
    onRemove: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(addon.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(
                "${addon.version} • ${addon.addonType.name}${if (addon.isBuiltIn) " • Built-in" else ""}",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        Switch(
            checked = addon.isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = NetflixRed)
        )
    }
}
