package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TacticalColorScheme = darkColorScheme(
    primary = SignalAmber,
    onPrimary = DeepGraphite,
    primaryContainer = FieldOlive,
    onPrimaryContainer = WeatheredIvory,
    secondary = CoolSteel,
    onSecondary = DeepGraphite,
    secondaryContainer = DeepGraphiteElevated,
    onSecondaryContainer = WeatheredIvory,
    tertiary = MutedForestGreen,
    onTertiary = WeatheredIvory,
    background = DeepGraphite,
    onBackground = WeatheredIvory,
    surface = DeepGraphiteElevated,
    onSurface = WeatheredIvory,
    surfaceVariant = DeepGraphiteBorder,
    onSurfaceVariant = WarmSand,
    error = AlertRed,
    onError = Color.White
)

@Composable
fun EmberwatchTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TacticalColorScheme,
        typography = Typography,
        content = content
    )
}
