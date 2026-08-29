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
        val colors = NewDesignPaletteController.colors
        return when (state) {
            GlucoseState.NORMAL -> Brush.verticalGradient(listOf(colors.normalStart, colors.normalEnd))
            GlucoseState.HIGH -> Brush.verticalGradient(listOf(colors.highStart, colors.highEnd))
            GlucoseState.LOW -> Brush.verticalGradient(listOf(colors.lowStart, colors.lowEnd))
        }
    }

    fun getMainTextColor(state: GlucoseState): Color {
        val colors = NewDesignPaletteController.colors
        return when (state) {
            GlucoseState.NORMAL -> colors.normalEnd
            GlucoseState.HIGH -> colors.highEnd
            GlucoseState.LOW -> colors.lowEnd
        }
    }

    fun getStateBadgeColor(state: GlucoseState): Color {
        val colors = NewDesignPaletteController.colors
        return when (state) {
            GlucoseState.NORMAL -> colors.normalBadge
            GlucoseState.HIGH -> colors.highBadge
            GlucoseState.LOW -> colors.lowBadge
        }
    }

    fun getSelectedTabTextColor(state: GlucoseState): Color = when (state) {
        GlucoseState.NORMAL -> Color(0xFF3FDC96)
        GlucoseState.HIGH -> Color(0xFFD2381A)
        GlucoseState.LOW -> Color(0xFFF8B610)
    }

    val MaxBadgeColor = Color(0xFFEE9C17)
    val MinBadgeColor = Color(0xFFD93B17)
    val NormalChartColor = Color(0xFF29AF99)

    val TabSelectedBackground = Color(0x33FFFFFF)
    val TabUnselectedText = Color(0xCCFFFFFF)
}
