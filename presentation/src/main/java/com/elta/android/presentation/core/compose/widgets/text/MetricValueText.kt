package com.elta.android.presentation.core.compose.widgets.text

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import com.elta.android.presentation.R

private val EmptyMetricPattern = Regex("[\\s—–−-]*")
private val NumericMetricPattern = Regex(
    "([+−-]?\\d+(?:[.,]\\d+)?)\\s*(?:%|ХЕ|Ед\\.?|изм\\.?|мин\\.?|г|ккал)?(?:\\s*\\(\\d+%\\))?",
    RegexOption.IGNORE_CASE
)

/** Display policy only: the underlying numeric value remains available to calculations. */
internal fun String.isMissingOrZeroMetric(): Boolean {
    val value = trim()
    if (EmptyMetricPattern.matches(value)) return true
    val number = NumericMetricPattern.matchEntire(value)?.groupValues?.get(1)
        ?.replace(',', '.')?.replace('−', '-')?.toDoubleOrNull()
    return number == 0.0
}

/** Two rounded strokes retain the same proportions at every metric size. */
@Composable
internal fun RoundedMetricPlaceholder(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val thickness = minOf(size.height, size.width / 8.5f)
        val halfGap = size.width * 0.075f
        val radius = thickness / 2
        val centerY = size.height / 2
        drawLine(color, Offset(radius, centerY), Offset(size.width / 2 - halfGap - radius, centerY), thickness, StrokeCap.Round)
        drawLine(color, Offset(size.width / 2 + halfGap + radius, centerY), Offset(size.width - radius, centerY), thickness, StrokeCap.Round)
    }
}

@Composable
internal fun MetricValueText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Normal,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    softWrap: Boolean = false,
    overflow: TextOverflow = TextOverflow.Clip,
    style: TextStyle = LocalTextStyle.current
) {
    val valueLineHeight = if (lineHeight.isUnspecified) fontSize * 1.2f else lineHeight
    if (text.isMissingOrZeroMetric()) {
        val description = if (NumericMetricPattern.matches(text.trim())) text
            else stringResource(R.string.metric_value_missing)
        val density = LocalDensity.current
        val width = with(density) { fontSize.toDp() }.coerceAtLeast(16.dp)
        val height = with(density) { valueLineHeight.toDp() }
        Box(modifier.height(height).semantics { contentDescription = description }, contentAlignment = Alignment.Center) {
            RoundedMetricPlaceholder(color, Modifier.size(width, width / 8.5f))
        }
    } else {
        Text(
            text = text, modifier = modifier, color = color, fontSize = fontSize,
            fontWeight = fontWeight, lineHeight = valueLineHeight, letterSpacing = letterSpacing,
            textAlign = textAlign, maxLines = maxLines, softWrap = softWrap, overflow = overflow, style = style
        )
    }
}
