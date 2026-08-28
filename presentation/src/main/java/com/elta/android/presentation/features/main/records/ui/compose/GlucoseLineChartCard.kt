package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R
import java.util.Locale

data class GlucosePoint(
    val timeLabel: String,
    val value: Float
)

private const val CHART_MAX_GLUCOSE_VALUE = 40f

@Composable
fun GlucoseLineChartCard(
    isDarkTheme: Boolean = false,
    selectedPeriod: String = "6ч",
    onPeriodSelected: (String) -> Unit = {},
    onChartClick: () -> Unit = {},
    points: List<GlucosePoint> = emptyList(),
    designScale: Float = 1f,
    cardHeight: androidx.compose.ui.unit.Dp = 201.dp * designScale
) {
    var activePeriod by remember { mutableStateOf(selectedPeriod) }
    val periods = listOf("3 ч", "6 ч", "12 ч", "24 ч")

    val cardBg = if (isDarkTheme) GlucoseDashboardTheme.DarkCardBackground else GlucoseDashboardTheme.LightCardBackground
    val cardTextColor = if (isDarkTheme) Color.White else Color(0xFF17191F)
    val gridLineColor = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color(0xFFE3E3E3)
    val axisLabelColor = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color(0xFF878B93)

    val (filteredPoints, filteredTimeLabels) = remember(points, activePeriod) {
        filterPointsAndLabelsForPeriod(points, activePeriod)
    }
    val displayPoints = filteredPoints
    val displayTimeLabels = filteredTimeLabels

    val maxPointVal = displayPoints.maxOfOrNull { it.value } ?: 0f
    val maxVal = if (maxPointVal > 16f) 20f else 16f
    val yLabels = if (maxVal == 20f) listOf("20", "15", "10", "5", "0") else listOf("16", "12", "8", "4", "0")

    Box(
        modifier = Modifier
            .padding(start = 14.dp * designScale, top = 16.dp * designScale, end = 14.dp * designScale, bottom = 0.dp)
            .fillMaxWidth()
            .height(cardHeight)
            .clip(RoundedCornerShape(13.dp * designScale))
            .border(
                width = 1.dp,
                color = if (isDarkTheme) GlucoseDashboardTheme.DarkCardBorder else Color(0xFFE3E3E3),
                shape = RoundedCornerShape(13.dp * designScale)
            )
            .background(cardBg)
            .padding(
                start = 14.dp * designScale,
                top = 14.dp * designScale,
                end = 12.dp * designScale,
                bottom = 10.dp * designScale
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Date Dropdown & Time Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Picker Dropdown Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp * designScale))
                        .clickable { }
                        .padding(vertical = 2.dp * designScale, horizontal = 4.dp * designScale),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Сегодня",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cardTextColor
                    )
                    Spacer(modifier = Modifier.width(4.dp * designScale))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_down),
                        contentDescription = "Select Date",
                        tint = cardTextColor,
                        modifier = Modifier.height(14.dp)
                    )
                }

                // Time Filter Segmented Switcher (149x24 dp in Figma reference)
                Row(
                    modifier = Modifier
                        .height(24.dp * designScale)
                        .clip(RoundedCornerShape(7.dp * designScale))
                        .border(
                            width = 0.5.dp,
                            color = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color(0xFFBBBFCA),
                            shape = RoundedCornerShape(7.dp * designScale)
                        )
                        .padding(1.5.dp * designScale),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    periods.forEach { period ->
                        val isSelected = period == activePeriod || period.replace(" ", "") == activePeriod.replace(" ", "")
                        Box(
                            modifier = Modifier
                                .height(20.dp * designScale)
                                .clip(RoundedCornerShape(7.dp * designScale))
                                .background(
                                    if (isSelected) {
                                        if (isDarkTheme) Color(0xFF4A5366) else Color(0xFF3D4556)
                                    } else {
                                        Color.Transparent
                                    }
                                )
                                .clickable {
                                    activePeriod = period
                                    onPeriodSelected(period)
                                }
                                .padding(horizontal = 7.dp * designScale),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period,
                                fontSize = if (isSelected) 14.sp else 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else axisLabelColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp * designScale))

            // Graph Section with Y-Axis Scale on the Left + Canvas + X-Axis Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Y-Axis Scale Labels
                Column(
                    modifier = Modifier
                        .width(20.dp * designScale)
                        .fillMaxHeight()
                        .padding(bottom = 18.dp * designScale),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    yLabels.forEach { yVal ->
                        Text(
                            text = yVal,
                            fontSize = 12.sp,
                            color = axisLabelColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp * designScale))

                // Chart Canvas & Peak Badges
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onChartClick() }
                ) {
                    val maxPt = displayPoints.maxByOrNull { it.value }
                    val minPt = displayPoints.minByOrNull { it.value }
                    val maxIdx = if (maxPt != null) displayPoints.indexOf(maxPt) else -1
                    val minIdx = if (minPt != null) displayPoints.indexOf(minPt) else -1
                    val hasDistinctExtremes = displayPoints.size > 1 && minPt?.value != maxPt?.value

                    var maxOffset by remember { mutableStateOf<Offset?>(null) }
                    var minOffset by remember { mutableStateOf<Offset?>(null) }

                    Canvas(modifier = Modifier.matchParentSize()) {
                        val width = size.width
                        val height = size.height
                        val paddingBottom = (20.dp * designScale).toPx()
                        val chartWidth = width
                        val chartRightInset = (24.dp * designScale).toPx()
                        val usableChartWidth = chartWidth - chartRightInset
                        val chartHeight = height - paddingBottom

                        // Draw Horizontal Grid Lines
                        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                        yLabels.forEachIndexed { index, _ ->
                            val y = (index.toFloat() / (yLabels.size - 1)) * chartHeight
                            drawLine(
                                color = gridLineColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                pathEffect = pathEffect,
                                strokeWidth = (1.dp * designScale).toPx()
                            )
                        }

                        // Draw Curve Path
                        val linePoints = displayPoints.map { pt ->
                            val latestPointMinutes = displayPoints.maxOfOrNull { it.timeLabel.toMinutes() } ?: 0
                            val endMinutes = roundUpToHour(latestPointMinutes)
                            val startMinutes = endMinutes - periodToHours(activePeriod) * 60
                            val x = ((pt.timeLabel.toMinutes() - startMinutes).toFloat() /
                                (periodToHours(activePeriod) * 60)).coerceIn(0f, 1f) * usableChartWidth
                            val pointRadius = (6.dp * designScale).toPx()
                            val safeValue = pt.value.coerceIn(0f, maxVal)
                            val y = (chartHeight - (safeValue / maxVal) * chartHeight)
                                .coerceIn(pointRadius, chartHeight - pointRadius)
                            Offset(x, y)
                        }

                        if (linePoints.isNotEmpty()) {
                            for (i in 0 until linePoints.size - 1) {
                                val p1 = linePoints[i]
                                val p2 = linePoints[i + 1]
                                val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2, p1.y)
                                val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2, p2.y)
                                val segmentPath = Path().apply {
                                    moveTo(p1.x, p1.y)
                                    cubicTo(
                                        controlPoint1.x, controlPoint1.y,
                                        controlPoint2.x, controlPoint2.y,
                                        p2.x, p2.y
                                    )
                                }
                                val startValue = displayPoints[i].value
                                val endValue = displayPoints[i + 1].value
                                val segmentBrush = Brush.linearGradient(
                                    colors = listOf(
                                        glucoseLineColor(startValue),
                                        glucoseLineColor((startValue + endValue) / 2f),
                                        glucoseLineColor(endValue)
                                    ),
                                    start = p1,
                                    end = p2
                                )

                                drawPath(
                                    path = segmentPath,
                                    brush = segmentBrush,
                                    style = Stroke(width = (3.dp * designScale).toPx(), cap = StrokeCap.Round)
                                )
                            }

                            linePoints.forEachIndexed { idx, point ->
                                val value = displayPoints[idx].value
                                val dotColor = glucoseLineColor(value)
                                drawCircle(
                                    color = Color.White,
                                    radius = (6.dp * designScale).toPx(),
                                    center = point
                                )
                                drawCircle(
                                    color = dotColor,
                                    radius = (4.dp * designScale).toPx(),
                                    center = point
                                )
                            }

                            if (hasDistinctExtremes && maxIdx >= 0 && maxIdx < linePoints.size) {
                                maxOffset = linePoints[maxIdx]
                            }
                            if (hasDistinctExtremes && minIdx >= 0 && minIdx < linePoints.size && minIdx != maxIdx) {
                                minOffset = linePoints[minIdx]
                            }
                        }
                    }

                    if (displayPoints.isEmpty()) {
                        Text(
                            text = "Нет измерений за выбранный период",
                            fontSize = 13.sp,
                            color = axisLabelColor,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Dynamic Max Peak Badge
                    if (hasDistinctExtremes) maxPt?.let { maxItem ->
                        maxOffset?.let { pt ->
                            val xDp = with(LocalDensity.current) { pt.x.toDp() }
                            val yDp = with(LocalDensity.current) { pt.y.toDp() }
                            PeakBadge(
                                text = "max ${String.format(Locale.US, "%.1f", maxItem.value).replace('.', ',')}",
                                bgColor = GlucoseDashboardTheme.MaxBadgeColor,
                                modifier = Modifier.padding(
                                    start = (xDp - 58.dp).coerceAtLeast(4.dp),
                                    top = (if (yDp < 36.dp) yDp + 10.dp else yDp - 28.dp)
                                        .coerceIn(2.dp, 130.dp)
                                )
                            )
                        }
                    }

                    // Dynamic Min Peak Badge
                    if (hasDistinctExtremes) minPt?.let { minItem ->
                        if (minPt != maxPt) {
                            minOffset?.let { pt ->
                                val xDp = with(LocalDensity.current) { pt.x.toDp() }
                                val yDp = with(LocalDensity.current) { pt.y.toDp() }
                                PeakBadge(
                                    text = "min ${String.format(Locale.US, "%.1f", minItem.value).replace('.', ',')}",
                                    bgColor = GlucoseDashboardTheme.MinBadgeColor,
                                    modifier = Modifier.padding(
                                        start = (xDp - 58.dp).coerceAtLeast(4.dp),
                                        top = (yDp + 8.dp).coerceIn(2.dp, 145.dp)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // X-Axis Time Labels Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                displayTimeLabels.forEach { label ->
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = axisLabelColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun filterPointsAndLabelsForPeriod(
    rawPoints: List<GlucosePoint>,
    period: String
): Pair<List<GlucosePoint>, List<String>> {
    if (rawPoints.isEmpty()) {
        return emptyList<GlucosePoint>() to emptyList()
    }

    val hours = when (period) {
        "3ч" -> 3
        "6ч" -> 6
        "12ч" -> 12
        "24ч" -> 24
        else -> 6
    }

    val latestPointMinutes = rawPoints.maxOf { it.timeLabel.toMinutes() }
    val endMinutes = roundUpToHour(latestPointMinutes)
    val filtered = rawPoints.filter { it.timeLabel.toMinutes() >= endMinutes - hours * 60 }
    val labels = buildTimelineLabels(endMinutes, hours, period)

    return Pair(filtered, labels)
}

private fun roundUpToHour(minutes: Int): Int {
    return if (minutes % 60 == 0) minutes else ((minutes / 60) + 1) * 60
}

private fun periodToHours(period: String): Int = when (period.replace(" ", "")) {
    "3ч" -> 3
    "6ч" -> 6
    "12ч" -> 12
    "24ч" -> 24
    else -> 6
}

private fun glucoseLineColor(value: Float): Color = when {
    value <= 3.9f -> GlucoseDashboardTheme.MinBadgeColor
    value >= 10f -> GlucoseDashboardTheme.MaxBadgeColor
    else -> GlucoseDashboardTheme.NormalChartColor
}

private fun String.toMinutes(): Int {
    val parts = split(":")
    val hours = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return (hours * 60 + minutes).coerceIn(0, 24 * 60)
}

private fun buildTimelineLabels(endMinutes: Int, hours: Int, period: String): List<String> {
    val endHour = endMinutes / 60
    val step = when (period.replace(" ", "")) {
        "3ч", "6ч" -> 1
        "12ч" -> 2
        "24ч" -> 4
        else -> 1
    }
    return (endHour - hours + step..endHour step step).map { hour ->
        val normalizedHour = (hour % 24 + 24) % 24
        String.format(Locale.US, "%02d:00", normalizedHour)
    }
}

@Composable
private fun PeakBadge(
    text: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .widthIn(min = 68.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            maxLines = 1,
            softWrap = false,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
