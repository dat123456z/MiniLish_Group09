package com.example.minlish.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryPurple      = Color(0xFF4F6EF7)
val PrimaryPurpleLight = Color(0xFF7B93FF)
val BackgroundGray     = Color(0xFFF8F9FF)
val SurfaceWhite       = Color.White
val TextPrimary        = Color(0xFF1A1C2E)
val TextSecondary      = Color(0xFF44464F)
val BorderColor        = Color(0xFFBDBFC9)
val ChipSelected       = Color(0xFF4F6EF7)
val ChipUnselected     = Color(0xFFE8EAF6)
val LinkColor          = Color(0xFF4F6EF7)
val ErrorColor         = Color(0xFFBA1A1A)

private val LightColors = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE3FF),
    onPrimaryContainer = Color(0xFF001257),
    secondary = Color(0xFF5B5EA6),
    onSecondary = Color.White,
    background = BackgroundGray,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = ChipUnselected,
    onSurfaceVariant = TextSecondary,
    error = ErrorColor,
    outline = BorderColor
)

private val DarkColors = darkColorScheme(
    primary = PrimaryPurpleLight,
    onPrimary = Color(0xFF001257),
    primaryContainer = Color(0xFF23408E),
    onPrimaryContainer = Color(0xFFDDE3FF),
    secondary = Color(0xFFBEC2FF),
    onSecondary = Color(0xFF252B72),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2EC),
    surface = Color(0xFF1A1C23),
    onSurface = Color(0xFFE2E2EC),
    surfaceVariant = Color(0xFF44464F),
    onSurfaceVariant = Color(0xFFC5C6D0),
    error = Color(0xFFFFB4AB),
    outline = Color(0xFF8E9099)
)

@Composable
fun MinlishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}