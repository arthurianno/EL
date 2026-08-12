package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class GlucoseState {
    NORMAL, // Normal range (e.g. 4.8 mmol/L)
    HIGH,   // High level (e.g. 10.8 mmol/L)
    LOW     // Low level (e.g. 2.5 mmol/L)
}

object GlucoseDashboardTheme {
    val DarkBackground = Color(0xFF3D4556)
    val DarkCardBackground = Color(0xFF2E3444)
    val DarkCardBorder = Color(0xFF4A5366)
    val LightCardBackground = Color(0xFFFFFFFF)

    // Header Gradients according to screenshots
    fun getHeaderGradient(state: GlucoseState, isDarkTheme: Boolean = false): Brush {
        if (isDarkTheme) {
            return Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2C3545),
                    Color(0xFF3D4556)
                )
            )
        }
        return when (state) {
            GlucoseState.NORMAL -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF8CD8A7),
                    Color(0xFF3BB2B8),
                    Color(0xFF1CB0B8)
                )
            )
            GlucoseState.HIGH -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFD56B),
                    Color(0xFFFFA24C),
                    Color(0xFFF98B44)
                )
            )
            GlucoseState.LOW -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFA8A8),
                    Color(0xFFF85F73),
                    Color(0xFFE84562)
                )
            )
        }
    }

    fun getMainTextColor(state: GlucoseState): Color {
        return when (state) {
            GlucoseState.NORMAL -> Color(0xFF3BB2B8)
            GlucoseState.HIGH -> Color(0xFFFFA24C)
            GlucoseState.LOW -> Color(0xFFF85F73)
        }
    }

    val MaxBadgeColor = Color(0xFFFF9800)
    val MinBadgeColor = Color(0xFFFF5252)

    val TabSelectedBackground = Color(0x33FFFFFF)
    val TabUnselectedText = Color(0xCCFFFFFF)
}
