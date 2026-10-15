package com.elta.android.presentation.features.sync.connect.pin

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Original assets use a 1000 × 1500 canvas: normal meter top y=684;
// the enlarged LCD spans x=245..769 and y=373..725.
internal data class PinIllustrationLayout(val width: Dp, val top: Dp) {
    val height: Dp get() = width * 1.5f
    val meterTop: Dp get() = top + width * 0.684f
    val lcdTop: Dp get() = top + width * 0.373f
    val lcdBottom: Dp get() = top + width * 0.725f
}

internal fun normalPinIllustrationLayout(viewportWidth: Dp, viewportHeight: Dp): PinIllustrationLayout {
    val width = minOf(viewportWidth, ((viewportHeight - 264.dp) / 0.816f).coerceAtLeast(0.dp))
    return PinIllustrationLayout(width, viewportHeight - 88.dp - width * 1.5f)
}

internal fun enlargedPinIllustrationLayout(
    normal: PinIllustrationLayout,
    visibleHeight: Dp,
    headerBottom: Dp,
    footerHeight: Dp
): PinIllustrationLayout {
    val bodyTop = headerBottom + 12.dp
    // Fit the whole LCD above the measured action panel, including its glass edge.
    val lcdBottomLimit = visibleHeight - footerHeight - 16.dp
    val width = minOf(normal.width, ((lcdBottomLimit - bodyTop) / 0.586f).coerceAtLeast(0.dp))
    return PinIllustrationLayout(width, bodyTop - width * 0.139f)
}
