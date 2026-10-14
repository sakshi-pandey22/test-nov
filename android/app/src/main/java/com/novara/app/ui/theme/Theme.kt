package com.novara.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Professional Academic & Editorial Palette (Off-White / Ivory theme)
val BgIvory = Color(0xFFF9F8F5)       // Off-white / Warm Ivory base
val BgSurface = Color(0xFFFFFFFF)     // Crisp White cards
val BgSubtle = Color(0xFFF2F0EB)      // Warm parchment subtle background
val BgCardBorder = Color(0xFFE2DDD5)  // Clean 1px card boundary
val BgTag = Color(0xFFECE8E1)         // Neutral tag background

// Text Colors (High contrast, editorial ink)
val TextPrimary = Color(0xFF181C20)   // Deep Charcoal Ink
val TextSecondary = Color(0xFF4A5568) // Slate Secondary
val TextMuted = Color(0xFF718096)     // Muted Caption
val TextInverted = Color(0xFFFFFFFF)  // White on dark buttons

// Academic Accent Colors (Clean, non-neon, professional)
val AcademicNavy = Color(0xFF1E3A5F)  // Classic University Navy (Primary)
val AcademicNavyDark = Color(0xFF132742)
val AcademicSage = Color(0xFF2C5E48)  // Deep Forest / Sage Green
val AcademicAmber = Color(0xFFB45309) // Warm Ochre / Amber (Repair)
val AcademicAmberBg = Color(0xFFFFFBEB) // Repair background
val AcademicWine = Color(0xFF831843)  // Deep Wine / Crimson
val ErrorRed = Color(0xFFB91C1C)      // Professional Error Red
val ErrorRedBg = Color(0xFFFEF2F2)

private val AcademicLightColorScheme = lightColorScheme(
    primary = AcademicNavy,
    onPrimary = TextInverted,
    secondary = AcademicSage,
    onSecondary = TextInverted,
    tertiary = AcademicAmber,
    background = BgIvory,
    onBackground = TextPrimary,
    surface = BgSurface,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = TextInverted
)

@Composable
fun NovaraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AcademicLightColorScheme,
        content = content
    )
}
