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

/** Keeps the plot readable when landscape height is limited, including large font settings. */
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
    val denseMetrics = compactSummary || summaryWidth < 760.dp * textScale
    val inlineLegend = chartWidth >= 600.dp * textScale
    val summaryHeight = if (compactSummary) {
        120.dp * textScale.coerceAtMost(1.35f)
    } else {
        (if (shortScreen) 82.dp else 90.dp) * textScale.coerceAtMost(1.25f)
    }
    val topGap = if (shortScreen) 4.dp else 6.dp
    val bottomGap = if (shortScreen) 4.dp else 8.dp
    val availableChartHeight = screenHeight - topPadding - bottomPadding - 36.dp -
        topGap - bottomGap - summaryHeight
    val minimumChartHeight = 240.dp + 24.dp * (textScale - 1f)

    return ContinuousChartLayout(
        compactSummary = compactSummary,
        denseMetrics = denseMetrics,
        inlineLegend = inlineLegend,
        chartCardHeight = maxOf(availableChartHeight, minimumChartHeight),
        summaryHeight = summaryHeight,
        scrollContent = availableChartHeight < minimumChartHeight
    )
}
