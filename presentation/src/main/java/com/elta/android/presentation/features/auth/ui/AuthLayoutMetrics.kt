package com.elta.android.presentation.features.auth.ui

import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

internal data class AuthLayoutMetrics(
    val illustrationSize: IntSize,
    val formY: Int,
    val contentHeight: Int
)

/** Pixel measurements; the viewport already excludes the toolbar and window insets. */
internal fun calculateAuthLayoutMetrics(
    viewportHeight: Int,
    contentWidth: Int,
    headingHeight: Int,
    formHeight: Int,
    preferredIllustrationSize: IntSize,
    minimumIllustrationHeight: Int
): AuthLayoutMetrics {
    val preferredWidth = preferredIllustrationSize.width.coerceAtLeast(1)
    val preferredHeight = preferredIllustrationSize.height.coerceAtLeast(1)
    val widthScale = (contentWidth.toFloat() / preferredWidth).coerceIn(0f, 1f)
    val maximumHeight = (preferredHeight * widthScale).roundToInt()
    val minimumHeight = minimumIllustrationHeight.coerceIn(0, maximumHeight)
    val availableHeight = (viewportHeight - headingHeight - formHeight).coerceAtLeast(0)
    val imageHeight = availableHeight.coerceIn(minimumHeight, maximumHeight)
    val imageWidth = (imageHeight.toFloat() * preferredWidth / preferredHeight)
        .roundToInt().coerceIn(0, contentWidth)
    val formY = maxOf(headingHeight + imageHeight, viewportHeight - formHeight)

    return AuthLayoutMetrics(
        illustrationSize = IntSize(imageWidth, imageHeight),
        formY = formY,
        contentHeight = formY + formHeight
    )
}
