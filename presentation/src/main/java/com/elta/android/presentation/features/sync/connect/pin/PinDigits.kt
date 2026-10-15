package com.elta.android.presentation.features.sync.connect.pin

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope

private const val DigitThicknessRatio = 0.18f

private val digitSegments = mapOf(
    '0' to "abcdef", '1' to "bc", '2' to "abged", '3' to "abgcd", '4' to "fgbc",
    '5' to "afgcd", '6' to "afgecd", '7' to "abc", '8' to "abcdefg", '9' to "abfgcd"
)

internal fun DrawScope.drawPinCode(pin: String) {
    if (pin.isEmpty()) return
    val height = size.height * 0.5f
    val width = minOf(height * 0.6f, size.width / 3.2f)
    val gap = width * 0.1f
    // Centre the visible segments too: a leading "1" has no left-hand segments.
    val leftInset = if (pin.first() == '1') width * (1f - DigitThicknessRatio) else 0f
    val textWidth = pin.length * width + (pin.length - 1) * gap - leftInset
    val left = (size.width - textWidth) / 2f - leftInset
    pin.forEachIndexed { index, digit ->
        drawPinDigit(digit, Offset(left + index * (width + gap), (size.height - height) / 2f), width, height)
    }
}

private fun DrawScope.drawPinDigit(digit: Char, origin: Offset, width: Float, height: Float) {
    val thickness = width * DigitThicknessRatio
    val bevel = thickness / 2f
    val jointGap = height * 0.012f
    fun segment(x: Float, y: Float, length: Float, vertical: Boolean) {
        val path = Path()
        val points = arrayOf(
            Offset(bevel, 0f), Offset(length - bevel, 0f), Offset(length, bevel),
            Offset(length - bevel, thickness), Offset(bevel, thickness), Offset(0f, bevel)
        )
        points.forEachIndexed { index, point ->
            val px = origin.x + x + if (vertical) point.y else point.x
            val py = origin.y + y + if (vertical) point.x else point.y
            if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        drawPath(path, Color(0xFF414141))
    }
    // Tapered ends face each other across a narrow diagonal gap at each joint.
    val horizontalStart = bevel + jointGap
    val horizontalLength = width - thickness - 2f * jointGap
    val verticalLength = height / 2f - bevel - 2f * jointGap
    digitSegments[digit].orEmpty().forEach { id ->
        when (id) {
            'a' -> segment(horizontalStart, 0f, horizontalLength, false)
            'g' -> segment(horizontalStart, height / 2f - bevel, horizontalLength, false)
            'd' -> segment(horizontalStart, height - thickness, horizontalLength, false)
            'f' -> segment(0f, bevel + jointGap, verticalLength, true)
            'b' -> segment(width - thickness, bevel + jointGap, verticalLength, true)
            'e' -> segment(0f, height / 2f + jointGap, verticalLength, true)
            'c' -> segment(width - thickness, height / 2f + jointGap, verticalLength, true)
        }
    }
}
