package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * The dashboard reserves the bottom navigation area and divides the remaining working
 * space into a 60% gauge header and 40% chart area. Width then scales the gauge so the
 * central metric stays balanced on narrow and wide phones.
 */
internal data class GlucoseDashboardLayout(
    val horizontalScale: Float,
    val ringSize: Dp,
    val ringTopOffset: Dp,
    val headerHeight: Dp,
    val chartHeight: Dp,
    val navigationTopSpacing: Dp,
    val gaugeTopSpacing: Dp,
    /** Extra height that anchors the TIR/pill row near the bottom of the gradient header. */
    val lowerControlsExtraOffset: Dp
)

internal fun calculateGlucoseDashboardLayout(
    screenWidth: Dp,
    screenHeight: Dp
): GlucoseDashboardLayout {
    val height = screenHeight.value
    val baseRingSize = interpolateByHeight(height, 146f, 175f, 199f, 199f)
    val navigationTopSpacing = interpolateByHeight(height, 34f, 53f, 59f, 59f).dp
    val gaugeTopSpacing = interpolateByHeight(height, 16f, 18f, 17f, 17f).dp
    val baseRingTopOffset = interpolateByHeight(height, 0f, 5f, 17f, 17f)

    // 343dp is the usable width in the 375dp Figma reference after 16dp side insets.
    // Keep the gauge readable on the narrowest devices and allow it to grow on wide phones.
    val availableGaugeWidth = (screenWidth.value - 32f).coerceAtLeast(0f)
    val horizontalScale = (availableGaugeWidth / 343f).coerceIn(0.8f, 1.2f)
    val ringSize = (baseRingSize * horizontalScale).coerceIn(128f, 239f)

    // The Figma frames include a 72dp bottom navigation area. The remaining cell space
    // follows the 60/40 composition rule. The minimum keeps the compact 592dp layout
    // from clipping a gauge that has grown because of screen width.
    val workingHeight = (height - 72f).coerceAtLeast(0f)
    val minimumHeaderHeight = 300f + (ringSize - 146f).coerceAtLeast(0f)
    val headerHeight = max(minimumHeaderHeight, workingHeight * 0.60f).dp
    val chartHeight = (workingHeight * 0.40f - 88f).coerceIn(142f, 300f).dp
    val ringTopOffset = (baseRingTopOffset * horizontalScale).dp

    // In Figma the lower data row ends 16dp above the gradient edge. On tall phones the
    // 60% header gains height, so transfer that free space to this row instead of leaving
    // an expanding empty area below it.
    val lowerControlsExtraOffset = (
        headerHeight -
            navigationTopSpacing -
            (33.dp * horizontalScale) -
            gaugeTopSpacing -
            glucoseGaugeBaseHeight(ringSize.dp, ringTopOffset) -
            (16.dp * horizontalScale)
        ).coerceAtLeast(0.dp)

    return GlucoseDashboardLayout(
        horizontalScale = horizontalScale,
        ringSize = ringSize.dp,
        ringTopOffset = ringTopOffset,
        headerHeight = headerHeight,
        chartHeight = chartHeight,
        navigationTopSpacing = navigationTopSpacing,
        gaugeTopSpacing = gaugeTopSpacing,
        lowerControlsExtraOffset = lowerControlsExtraOffset
    )
}

/** Height of the gauge before the adaptive space that moves its lower data row down. */
internal fun glucoseGaugeBaseHeight(ringSize: Dp, ringTopOffset: Dp): Dp {
    val scaleFactor = ringSize.value / 199f
    return ringSize + ringTopOffset + glucoseGaugeBottomSectionHeight(ringSize) + 7.dp * scaleFactor
}

private fun glucoseGaugeBottomSectionHeight(ringSize: Dp): Dp {
    val diameter = ringSize.value
    val height = when {
        diameter <= 146f -> 71f
        diameter <= 175f -> 71f + (diameter - 146f) * 10f / 29f
        diameter <= 199f -> 81f + (diameter - 175f) * 19f / 24f
        else -> 100f + (diameter - 199f) * 100f / 199f
    }
    return height.dp
}

internal fun glucoseValueFontSize(ringSize: Dp): Float {
    val diameter = ringSize.value
    return when {
        diameter <= 146f -> 58f
        diameter <= 175f -> lerp(58f, 65f, (diameter - 146f) / (175f - 146f))
        diameter <= 199f -> lerp(65f, 75f, (diameter - 175f) / (199f - 175f))
        else -> 75f + (diameter - 199f) * (75f / 199f)
    }
}

/** Keeps multi-digit glucose values inside the inner disc without reducing short values. */
internal fun fittedGlucoseValueFontSize(
    value: String,
    ringSize: Dp,
    discWidth: Dp
): Float {
    val baseSize = glucoseValueFontSize(ringSize)
    val glyphUnits = value.sumOf { symbol ->
        when {
            symbol.isDigit() -> 1.0
            symbol == ',' || symbol == '.' -> 0.35
            else -> 0.7
        }
    }.toFloat().coerceAtLeast(1f)
    val maxSizeForDisc = discWidth.value * 0.86f / (glyphUnits * 0.7f)
    return minOf(baseSize, maxSizeForDisc)
}

private fun interpolateByHeight(
    height: Float,
    compact: Float,
    medium: Float,
    regular: Float,
    tall: Float
): Float = when {
    height <= 592f -> compact
    height <= 716f -> lerp(compact, medium, (height - 592f) / (716f - 592f))
    height <= 812f -> lerp(medium, regular, (height - 716f) / (812f - 716f))
    height <= 966f -> lerp(regular, tall, (height - 812f) / (966f - 812f))
    else -> tall
}

private fun lerp(start: Float, end: Float, fraction: Float): Float =
    start + (end - start) * fraction.coerceIn(0f, 1f)
