package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FocusCyan,
    secondary = BrightGold,
    tertiary = SoftGray,
    background = MidnightBlue,
    surface = DeepCharcoal,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for TV
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
