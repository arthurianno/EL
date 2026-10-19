package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class ContinuousChartLayout(
    val compactSummary: Boolean,
    val denseMetrics: Boolean,
    val inlineLegend: Boolean,
    val chartCardHeight: Dp,
    val summaryHeight: Dp,
    val scrollContent: Boolean
)

/** Fits ordinary landscape phones; keeps scrolling as a fallback for accessibility text sizes. */
internal fun continuousChartLayout(
    screenWidth: Dp,
    screenHeight: Dp,
    sideMargin: Dp,
    topPadding: Dp,
    bottomPadding: Dp,
    fontScale: Float
): ContinuousChartLayout {
    val textScale = fontScale.coerceIn(1f, 1.5f)
    val shortScreen = screenHeight < 420.dp
    val summaryWidth = screenWidth - sideMargin * 2 - 40.dp
    val chartWidth = screenWidth - sideMargin * 2 - 28.dp
    val compactSummary = summaryWidth < 590.dp * textScale
    val denseMetrics = compactSummary || summaryWidth < 660.dp * textScale
    val inlineLegend = chartWidth >= 600.dp * textScale
    val summaryHeight = if (compactSummary) {
        120.dp * textScale.coerceAtMost(1.35f)
    } else {
        (if (shortScreen) 74.dp else 82.dp) * textScale.coerceAtMost(1.25f)
    }
    val topGap = if (shortScreen) 4.dp else 6.dp
    val bottomGap = if (shortScreen) 4.dp else 8.dp
    val availableChartHeight = screenHeight - topPadding - bottomPadding - 32.dp -
        topGap - bottomGap - summaryHeight
    val minimumChartHeight = (if (inlineLegend) 196.dp else 220.dp) +
        40.dp * (textScale - 1f)

    return ContinuousChartLayout(
        compactSummary = compactSummary,
        denseMetrics = denseMetrics,
        inlineLegend = inlineLegend,
        chartCardHeight = maxOf(availableChartHeight, minimumChartHeight),
        summaryHeight = summaryHeight,
        scrollContent = availableChartHeight < minimumChartHeight
    )
}
