package com.homeflix.tv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/**
 * Top Navigation Bar — OVERLAID on top of the hero (transparent).
 *
 * Matches the reference UI:
 *   [Brand "N" logo]  [Search icon]  Home  Shows  Movies  My Netflix
 *
 * - Transparent background (no solid dark — hero shows through)
 * - Active tab: white text, slightly bolder
 * - Focused tab: subtle highlight
 * - 150ms animated transitions
 */

val NAV_BAR_HEIGHT = 56.dp

@Composable
fun TopNavBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(NAV_BAR_HEIGHT)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Left: NETFLIX wordmark + nav links ───────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("NETFLIX", color = Color(0xFFE50914), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            NavTextItem(label = "Home", isSelected = selectedRoute == "Home", onSelect = { onNavigate("Home") })
            NavTextItem(label = "Movies", isSelected = selectedRoute == "Movies", onSelect = { onNavigate("Movies") })
            NavTextItem(label = "TV Shows", isSelected = selectedRoute == "TV Shows", onSelect = { onNavigate("TV Shows") })
            NavTextItem(label = "My List", isSelected = selectedRoute == "My List", onSelect = { onNavigate("My List") })
        }

        // ── Right: Search + Settings icons ───────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NavIconButton(Icons.Filled.Search, "Search", onSearch)
            NavIconButton(Icons.Filled.Settings, "Settings", onSettings)
        }
    }
}

/** Focusable icon button for the nav bar (Search / Settings). */
@Composable
private fun NavIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        scale = CardDefaults.scale(focusedScale = 1.1f),
        shape = CardDefaults.shape(shape = CircleShape),
        colors = CardDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color(0x33FFFFFF)
        ),
        border = CardDefaults.border(
            focusedBorder = Border(BorderStroke(1.dp, Color(0x66FFFFFF)), shape = CircleShape)
        )
    ) {
        Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription, tint = Color(0xFFEEEEEE), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun NavTextItem(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color.White
            isFocused -> Color(0xFFE0E0E0)
            else -> Color(0xB3FFFFFF) // ~70% opacity
        },
        animationSpec = tween(150),
        label = "nav_text_color"
    )

    Card(
        onClick = onSelect,
        scale = CardDefaults.scale(focusedScale = 1f),
        shape = CardDefaults.shape(shape = RoundedCornerShape(4.dp)),
        colors = CardDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent
        ),
        border = CardDefaults.border(border = Border.None, focusedBorder = Border.None),
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .onFocusChanged { isFocused = it.isFocused }
    ) {
        Text(
            label,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
    }
}
