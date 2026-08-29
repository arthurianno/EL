package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Geometry derived from the four approved Figma frames (375 x 592/716/812/966).
 *
 * The height controls the compact/regular composition. Width then scales the complete
 * gauge proportionally, so the central metric stays balanced on narrow and wide phones.
 */
internal data class GlucoseDashboardLayout(
    val horizontalScale: Float,
    val ringSize: Dp,
    val ringTopOffset: Dp,
    val headerHeight: Dp,
    val chartHeight: Dp,
    val navigationTopSpacing: Dp,
    val gaugeTopSpacing: Dp
)

internal fun calculateGlucoseDashboardLayout(
    screenWidth: Dp,
    screenHeight: Dp
): GlucoseDashboardLayout {
    val height = screenHeight.value
    val baseRingSize = interpolateByHeight(height, 146f, 175f, 199f, 199f)
    val baseHeaderHeight = interpolateByHeight(height, 312f, 381f, 440f, 506f)
    val chartHeight = interpolateByHeight(height, 142f, 164f, 201f, 272f).dp
    val navigationTopSpacing = interpolateByHeight(height, 34f, 53f, 59f, 59f).dp
    val gaugeTopSpacing = interpolateByHeight(height, 16f, 18f, 17f, 17f).dp
    val baseRingTopOffset = interpolateByHeight(height, 0f, 5f, 17f, 17f)

    // 343dp is the usable width in the 375dp Figma reference after 16dp side insets.
    // Keep the gauge readable on the narrowest devices and allow it to grow on wide phones.
    val availableGaugeWidth = (screenWidth.value - 32f).coerceAtLeast(0f)
    val horizontalScale = (availableGaugeWidth / 343f).coerceIn(0.8f, 1.2f)
    val ringSize = (baseRingSize * horizontalScale).coerceIn(128f, 239f)

    return GlucoseDashboardLayout(
        horizontalScale = horizontalScale,
        ringSize = ringSize.dp,
        ringTopOffset = (baseRingTopOffset * horizontalScale).dp,
        // A wider ring requires exactly that additional vertical room; fixed controls keep
        // their Figma dimensions and therefore do not contribute to this delta.
        headerHeight = max(300f, baseHeaderHeight + ringSize - baseRingSize).dp,
        chartHeight = chartHeight,
        navigationTopSpacing = navigationTopSpacing,
        gaugeTopSpacing = gaugeTopSpacing
    )
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
