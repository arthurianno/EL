package com.elta.android.presentation.features.sync.connect.pin

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Original assets use a 1000 × 1500 canvas: normal meter top y=684;
// the enlarged LCD spans x=245..769 and y=373..725.
internal data class PinIllustrationLayout(val width: Dp, val top: Dp) {
    val height: Dp get() = width * 1.5f
    val meterTop: Dp get() = top + width * 0.684f
    val lcdTop: Dp get() = top + width * 0.373f
}

internal fun normalPinIllustrationLayout(viewportWidth: Dp, viewportHeight: Dp): PinIllustrationLayout {
    val width = minOf(viewportWidth, ((viewportHeight - 264.dp) / 0.816f).coerceAtLeast(0.dp))
    return PinIllustrationLayout(width, viewportHeight - 88.dp - width * 1.5f)
}

internal fun enlargedPinIllustrationLayout(
    normal: PinIllustrationLayout,
    visibleHeight: Dp,
    fontScale: Float
): PinIllustrationLayout {
    val bodyTop = minOf(48.dp + 112.dp * fontScale + 12.dp, visibleHeight * 0.45f)
    val width = minOf(normal.width, ((visibleHeight - 80.dp - bodyTop) / 0.586f).coerceAtLeast(0.dp))
    return PinIllustrationLayout(width, bodyTop - width * 0.139f)
}
