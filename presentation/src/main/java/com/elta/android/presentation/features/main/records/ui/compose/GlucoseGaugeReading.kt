package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.core.compose.widgets.text.MetricValueText

/** Measures the real font inside the disc; the three rows never share vertical space. */
@Composable
internal fun GlucoseGaugeReading(
    value: String,
    unit: String,
    trend: GlucoseTrend?,
    fallbackTrend: String,
    color: Color,
    maximumValueFontSize: Float,
    designScale: Float
) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val textStyle = LocalTextStyle.current.copy(
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val captionSize = (12f * designScale).coerceAtLeast(10f).sp
    val captionLineHeight = captionSize * 1.2f
    val rowGap = (2.dp * designScale).coerceAtLeast(1.5.dp)
    BoxWithConstraints(Modifier.fillMaxSize().testTag("glucose_gauge_reading")) {
        val contentWidth = with(density) { maxWidth.toPx() * .86f }
        val numberHeight = with(density) {
            maxHeight.toPx() * .84f - captionLineHeight.toPx() * 2 - rowGap.toPx() * 2 - 2
        }.coerceAtLeast(1f)
        val valueSize = remember(value, contentWidth, numberHeight, maximumValueFontSize, textStyle, measurer) {
            fun fits(size: Float): Boolean {
                val measured = measurer.measure(
                    text = value,
                    style = textStyle.copy(
                        fontSize = size.sp, lineHeight = (size * 1.08f).sp,
                        fontWeight = FontWeight.Bold, letterSpacing = (-.067f).em
                    ),
                    maxLines = 1, softWrap = false
                )
                return measured.size.width <= contentWidth && measured.size.height <= numberHeight
            }
            if (fits(maximumValueFontSize)) maximumValueFontSize else {
                var minimum = 8f
                var maximum = maximumValueFontSize
                repeat(12) {
                    val candidate = (minimum + maximum) / 2
                    if (fits(candidate)) minimum = candidate else maximum = candidate
                }
                minimum
            }
        }
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(.86f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(rowGap)
        ) {
            MetricValueText(
                text = value, color = color, fontSize = valueSize.sp,
                lineHeight = (valueSize * 1.08f).sp, fontWeight = FontWeight.Bold,
                letterSpacing = (-.067f).em, style = textStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().testTag("glucose_gauge_value")
            )
            Text(
                text = unit, color = color, fontSize = captionSize,
                lineHeight = captionLineHeight, style = textStyle, maxLines = 1,
                modifier = Modifier.testTag("glucose_gauge_unit")
            )
            Row(
                modifier = Modifier.testTag("glucose_gauge_trend"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val trendColor = Color(0xFFBBBFCA)
                if (trend != null && trend.direction != GlucoseTrendDirection.STABLE) {
                    val arrowHeight = with(density) { captionSize.toDp() * .6f }
                    Canvas(Modifier.size(arrowHeight * 1.6f, arrowHeight)) {
                        val path = Path().apply {
                            if (trend.direction == GlucoseTrendDirection.DOWN) {
                                moveTo(size.width / 2, size.height)
                                lineTo(size.width, 0f)
                                lineTo(0f, 0f)
                            } else {
                                moveTo(size.width / 2, 0f)
                                lineTo(size.width, size.height)
                                lineTo(0f, size.height)
                            }
                            close()
                        }
                        drawPath(path, trendColor)
                    }
                    Spacer(Modifier.width(3.dp * designScale))
                }
                MetricValueText(
                    text = trend?.valueText ?: fallbackTrend,
                    color = trendColor, fontSize = captionSize, lineHeight = captionLineHeight,
                    fontWeight = FontWeight.Bold, style = textStyle
                )
            }
        }
    }
}
