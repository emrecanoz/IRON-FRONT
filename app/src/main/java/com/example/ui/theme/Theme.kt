package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IronFrontColorScheme = darkColorScheme(
    primary = TacticalGreen,
    onPrimary = Color(0xFF022C22),
    primaryContainer = TacticalGreenSoft,
    onPrimaryContainer = TacticalGreen,
    secondary = EconomyGold,
    onSecondary = Color(0xFF1E1B18),
    secondaryContainer = EconomyGoldSoft,
    onSecondaryContainer = EconomyGoldLight,
    tertiary = ShieldCyan,
    onTertiary = Color(0xFF042F2E),
    tertiaryContainer = ShieldCyanSoft,
    onTertiaryContainer = ShieldCyanLight,
    error = DangerRed,
    onError = Color.White,
    errorContainer = DangerRedSoft,
    onErrorContainer = DangerRed,
    background = SlateBackground,
    onBackground = TextPrimary,
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IronFrontColorScheme,
        typography = Typography,
        content = content
    )
}
