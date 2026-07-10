package com.waffensachkunde.trainer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GoldAccent = Color(0xFFC9A227)
private val DarkPrimary = Color(0xFF1B2430)

private val LightColors = lightColorScheme(
    primary = DarkPrimary,
    secondary = GoldAccent,
    tertiary = GoldAccent
)

private val DarkColors = darkColorScheme(
    primary = GoldAccent,
    secondary = GoldAccent,
    tertiary = DarkPrimary
)

@Composable
fun WaffensachkundeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
