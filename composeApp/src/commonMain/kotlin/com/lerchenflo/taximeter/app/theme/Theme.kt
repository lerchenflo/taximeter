package com.lerchenflo.taximeter.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

val Bg = Color(0xFF0E0F10)
val Surface = Color(0xFF17191B)
val Surface2 = Color(0xFF1F2123)
val Line = Color(0x12FFFFFF)
val LineHi = Color(0x1FFFFFFF)
val TextPrimary = Color(0xFFF2EEE7)
val TextSecondary = Color(0xFF9C9891)
val TextTertiary = Color(0xFF5E5B56)
val Accent = Color(0xFFF0A24B)
val AccentDim = Color(0x23F0A24B)
val AccentLine = Color(0x66F0A24B)
val Live = Color(0xFF7AD4A5)
val LiveDim = Color(0x237AD4A5)
val Red = Color(0xFFE77271)
val OnAccent = Color(0xFF1A0F02)
val Mono = FontFamily.Monospace

private val TaximeterColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    primaryContainer = AccentDim,
    onPrimaryContainer = Accent,
    secondary = Live,
    onSecondary = OnAccent,
    background = Bg,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextSecondary,
    outline = Line,
    outlineVariant = LineHi,
    error = Red,
    onError = OnAccent,
)

@Composable
fun TaximeterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TaximeterColorScheme,
        content = content,
    )
}
