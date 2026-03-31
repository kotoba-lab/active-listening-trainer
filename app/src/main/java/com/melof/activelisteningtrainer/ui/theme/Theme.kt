package com.melof.activelisteningtrainer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary   = Color(0xFF4A7C59),   // グリーン系（傾聴・受容のイメージ）
    secondary = Color(0xFF6BA07A),
    background = Color(0xFFF5F7F5),
    surface    = Color(0xFFFFFFFF),
    error      = Color(0xFFB00020)
)

@Composable
fun ActiveListeningTrainerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
