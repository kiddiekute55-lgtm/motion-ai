package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MotionAiDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    secondary = VioletAi,
    onSecondary = Color.White,
    tertiary = AccentPink,
    background = DarkSlate,
    onBackground = TextLight,
    surface = CardSurface,
    onSurface = TextLight
)

@Composable
fun MotionAiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MotionAiDarkColorScheme,
        typography = Typography,
        content = content
    )
}
