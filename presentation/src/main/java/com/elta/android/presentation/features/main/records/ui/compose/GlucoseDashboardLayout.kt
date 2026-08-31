package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * The dashboard receives the actual space above the app navigation. It divides that
 * space into a 60% gauge header and 40% chart area, while retaining the 72dp Figma
 * navigation reference only for the ring's visual breakpoints.
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
    availableContentHeight: Dp
): GlucoseDashboardLayout {
    // The design references include a 72dp app navigation. The measured content area has
    // already excluded it, so restore it only when selecting the matching Figma breakpoint.
    val designReferenceHeight = availableContentHeight.value + 72f
    val baseRingSize = interpolateByHeight(designReferenceHeight, 146f, 175f, 199f, 199f)
    val navigationTopSpacing = interpolateByHeight(designReferenceHeight, 34f, 53f, 59f, 59f).dp
    val gaugeTopSpacing = interpolateByHeight(designReferenceHeight, 16f, 18f, 17f, 17f).dp
    val baseRingTopOffset = interpolateByHeight(designReferenceHeight, 0f, 5f, 17f, 17f)

    // 343dp is the usable width in the 375dp Figma reference after 16dp side insets.
    // Keep the gauge readable on the narrowest devices and allow it to grow on wide phones.
    val availableGaugeWidth = (screenWidth.value - 32f).coerceAtLeast(0f)
    val horizontalScale = (availableGaugeWidth / 343f).coerceIn(0.8f, 1.2f)
    val widthDrivenRingSize = (baseRingSize * horizontalScale).coerceIn(128f, 239f)

    // This is the real viewport above the measured navigation menu and system controls.
    // Do not subtract a fixed navigation height here: it differs across gesture and
    // three-button modes, especially on MIUI.
    val workingHeight = availableContentHeight.value.coerceAtLeast(0f)
    val ringTopOffset = (baseRingTopOffset * horizontalScale).dp
    val tabHeight = 33.dp * horizontalScale
    val ringSize = if (horizontalScale <= 1f) {
        // Preserve the compact Figma frames unchanged. The fitting rule below is
        // specifically for wider devices whose width would otherwise enlarge the ring.
        widthDrivenRingSize.dp
    } else {
        fitRingSizeToHeader(
            widthDrivenRingSize = widthDrivenRingSize.dp,
            workingHeight = workingHeight.dp,
            navigationTopSpacing = navigationTopSpacing,
            tabHeight = tabHeight,
            gaugeTopSpacing = gaugeTopSpacing,
            ringTopOffset = ringTopOffset
        )
    }
    val minimumHeaderHeight = 300f + (ringSize.value - 146f).coerceAtLeast(0f)
    val headerHeight = max(minimumHeaderHeight, workingHeight * 0.60f).dp
    val chartHeight = (workingHeight * 0.40f - 88f).coerceIn(142f, 300f).dp

    // In Figma the lower data row ends 16dp above the gradient edge. On tall phones the
    // 60% header gains height, so transfer that free space to this row instead of leaving
    // an expanding empty area below it.
    val lowerControlsExtraOffset = (
        headerHeight -
            navigationTopSpacing -
            tabHeight -
            gaugeTopSpacing -
            glucoseGaugeBaseHeight(ringSize, ringTopOffset) -
            16.dp
        ).coerceAtLeast(0.dp)

    return GlucoseDashboardLayout(
        horizontalScale = horizontalScale,
        ringSize = ringSize,
        ringTopOffset = ringTopOffset,
        headerHeight = headerHeight,
        chartHeight = chartHeight,
        navigationTopSpacing = navigationTopSpacing,
        gaugeTopSpacing = gaugeTopSpacing,
        lowerControlsExtraOffset = lowerControlsExtraOffset
    )
}

/**
 * Width may suggest a larger circle than the 60%-high header can safely contain.
 * Constrain it by the actual available height so the lower metrics always retain
 * their Figma 16dp bottom inset instead of being clipped by the chart.
 */
private fun fitRingSizeToHeader(
    widthDrivenRingSize: Dp,
    workingHeight: Dp,
    navigationTopSpacing: Dp,
    tabHeight: Dp,
    gaugeTopSpacing: Dp,
    ringTopOffset: Dp
): Dp {
    fun fits(candidate: Dp): Boolean {
        val headerHeight = max(
            300f + (candidate.value - 146f).coerceAtLeast(0f),
            workingHeight.value * 0.60f
        ).dp
        val requiredHeaderHeight = navigationTopSpacing +
            tabHeight +
            gaugeTopSpacing +
            glucoseGaugeBaseHeight(candidate, ringTopOffset) +
            16.dp
        return headerHeight >= requiredHeaderHeight
    }

    if (fits(widthDrivenRingSize)) return widthDrivenRingSize

    var low = 128f
    var high = widthDrivenRingSize.value
    repeat(20) {
        val candidate = ((low + high) / 2f).dp
        if (fits(candidate)) low = candidate.value else high = candidate.value
    }
    return low.dp
}

/** Height of the gauge before the adaptive space that moves its lower data row down. */
internal fun glucoseGaugeBaseHeight(ringSize: Dp, ringTopOffset: Dp): Dp {
    // At the 812dp Figma reference the lower metrics group begins at y=367 and
    // ends at y=424, leaving the specified 16dp inset before the 440dp gradient edge.
    // The former extra 7dp shifted this group into the chart on compact MIUI layouts.
    return ringSize + ringTopOffset + glucoseGaugeBottomSectionHeight(ringSize)
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
