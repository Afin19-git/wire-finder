package com.afin19.wirefinder.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ToolDarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = AmberOnPrimary,
    primaryContainer = AmberPrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = CyanAccent,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = ElectricBlue,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder
)

@Composable
fun WireFinderTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ToolDarkColorScheme,
        typography = Typography,
        content = content
    )
}
