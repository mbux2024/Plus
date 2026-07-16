package com.homeflix.tv.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// STREAMBERT DESIGN SYSTEM — Prime/Netflix hybrid (ported from the Plus app).
//
// A deep blue-black canvas like Prime Video, with Prime blue as the interactive
// accent and Netflix red kept as the brand color. These top-level vals are the
// app-wide tokens; repointing them here restyles every screen that references
// them (directly or via the MaterialTheme colorScheme below).
// ─────────────────────────────────────────────────────────────────────────────

// Core canvas (Prime blue-black)
val Background = Color(0xFF0F171E)        // page background (was pure-black)
val BackgroundDeep = Color(0xFF00050D)    // hero / gradient target
val Surface = Color(0xFF1A242F)           // cards, chips
val SurfaceVariant = Color(0xFF252E39)    // focused / raised surface

// Brand + accents
val Red = Color(0xFFE50914)               // Netflix-red brand (wordmark, progress)
val PrimeBlue = Color(0xFF1399FF)         // CTA / focus accent
val PrimeBlueDark = Color(0xFF0F6FBD)

// Text
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF8197A4)     // Prime blue-grey secondary
val TextTertiary = Color(0xFF5C6B78)

// Component accents
val RatingGold = Color(0xFFFFB43A)        // star ratings
val BadgeOutline = Color(0xFF3A4750)      // certification chip border
val Top10Stroke = Color(0xFF425364)       // outlined big rank numbers
val FocusBorder = Color(0xFFFFFFFF)       // focused card border
val CardBackground = Color(0xFF1A242F)    // poster placeholder bg
