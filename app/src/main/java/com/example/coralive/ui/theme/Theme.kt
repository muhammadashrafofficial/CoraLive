package com.example.coralive.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class CoraColors(
    val background: Color = Color(0xFF08080A),
    val surface: Color = Color(0xFF121215),
    val surfaceElevated: Color = Color(0xFF1A1A1F),
    val border: Color = Color(0xFF26262C),
    val homeAccent: Color = Color(0xFF38BDF8),
    val awayAccent: Color = Color(0xFFF472B6),
    val liveRed: Color = Color(0xFFFF3B30),
    val textPrimary: Color = Color(0xFFF5F5F7),
    val textSecondary: Color = Color(0xFF8E8E93),
    val pillActiveBg: Color = Color(0xFFFFFFFF),
    val pillActiveText: Color = Color(0xFF08080A),
    val dotInactive: Color = Color(0xFF2C2C32)
)

val LocalCoraColors = staticCompositionLocalOf { CoraColors() }

object CoraTheme {
    val colors: CoraColors
        @Composable get() = LocalCoraColors.current

    val cardBorder: BorderStroke
        @Composable get() = BorderStroke(1.dp, colors.border)
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    secondary = Color(0xFFF472B6),
    tertiary = Color(0xFFFF3B30),
    background = Color(0xFF08080A),
    surface = Color(0xFF121215),
    surfaceVariant = Color(0xFF1A1A1F),
    onPrimary = Color(0xFF08080A),
    onSecondary = Color(0xFF08080A),
    onBackground = Color(0xFFF5F5F7),
    onSurface = Color(0xFFF5F5F7),
    outline = Color(0xFF26262C)
)

private val MonoTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 1.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        letterSpacing = 1.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp
    )
)

@Composable
fun CoraLiveTheme(content: @Composable () -> Unit) {
    val coraColors = CoraColors()
    CompositionLocalProvider(LocalCoraColors provides coraColors) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = MonoTypography,
            content = content
        )
    }
}