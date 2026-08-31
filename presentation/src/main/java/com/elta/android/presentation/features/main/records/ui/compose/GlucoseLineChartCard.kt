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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R
import java.util.Locale
import kotlin.math.roundToInt

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

    // Do not scale the whole chart on tap: a fractional graphics layer rasterizes the
    // dynamic min/max labels and makes their text blurry on some Android renderers.
    fun handleChartClick() = onChartClick()

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
            .padding(start = 17.dp * designScale, top = 16.dp * designScale, end = 17.dp * designScale, bottom = 0.dp)
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
                start = 8.dp * designScale,
                top = 14.dp * designScale,
                end = 12.dp * designScale,
                bottom = 10.dp * designScale
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Date Dropdown & Time Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp * designScale),
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
                        .width(16.dp * designScale)
                        .fillMaxHeight(),
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

                Spacer(modifier = Modifier.width(5.5.dp * designScale))

                // Chart Canvas & Peak Badges
                var chartSize by remember { mutableStateOf(IntSize.Zero) }
                var maxBadgeSize by remember { mutableStateOf(IntSize.Zero) }
                var minBadgeSize by remember { mutableStateOf(IntSize.Zero) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .onSizeChanged { chartSize = it }
                        .clickable { handleChartClick() }
                ) {
                    val maxPt = displayPoints.maxByOrNull { it.value }
                    val minPt = displayPoints.minByOrNull { it.value }
                    val maxIdx = if (maxPt != null) displayPoints.indexOf(maxPt) else -1
                    val minIdx = if (minPt != null) displayPoints.indexOf(minPt) else -1
                    val hasDistinctExtremes = displayPoints.size > 1 && minPt?.value != maxPt?.value

                    val density = LocalDensity.current
                    val chartPoints = remember(
                        displayPoints,
                        activePeriod,
                        chartSize,
                        maxVal,
                        designScale,
                        density
                    ) {
                        calculateChartPointOffsets(
                            points = displayPoints,
                            activePeriod = activePeriod,
                            chartWidth = chartSize.width.toFloat(),
                            chartHeight = chartSize.height.toFloat(),
                            maxValue = maxVal,
                            pointRadiusPx = with(density) { (6.dp * designScale).toPx() },
                            rightInsetPx = with(density) { (24.dp * designScale).toPx() }
                        )
                    }
                    val peakBadgePlacements = remember(
                        chartSize,
                        chartPoints,
                        minIdx,
                        maxIdx,
                        minBadgeSize,
                        maxBadgeSize,
                        density,
                        designScale
                    ) {
                        calculatePeakBadgePlacements(
                            chartSize = chartSize,
                            minPoint = chartPoints.getOrNull(minIdx),
                            maxPoint = chartPoints.getOrNull(maxIdx),
                            minBadgeSize = minBadgeSize,
                            maxBadgeSize = maxBadgeSize,
                            edgePx = with(density) { (4.dp * designScale).roundToPx() },
                            gapPx = with(density) { (8.dp * designScale).roundToPx() }
                        )
                    }

                    Canvas(modifier = Modifier.matchParentSize()) {
                        val width = size.width
                        val height = size.height
                        val chartWidth = width
                        val chartRightInset = (24.dp * designScale).toPx()
                        val chartHeight = height

                        // Draw Vertical Axis Line (Vector 26 in Figma)
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, 0f),
                            end = Offset(0f, chartHeight),
                            strokeWidth = (1.dp * designScale).toPx()
                        )

                        // Draw Horizontal Grid Lines
                        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                        yLabels.forEachIndexed { index, _ ->
                            val y = (index.toFloat() / (yLabels.size - 1)) * chartHeight
                            val isBottomBaseline = index == yLabels.lastIndex
                            drawLine(
                                color = gridLineColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                pathEffect = if (isBottomBaseline) null else pathEffect,
                                strokeWidth = (1.dp * designScale).toPx()
                            )
                        }

                        // Draw Curve Path
                        val linePoints = calculateChartPointOffsets(
                            points = displayPoints,
                            activePeriod = activePeriod,
                            chartWidth = chartWidth,
                            chartHeight = chartHeight,
                            maxValue = maxVal,
                            pointRadiusPx = (6.dp * designScale).toPx(),
                            rightInsetPx = chartRightInset
                        )

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
                        PeakBadge(
                            text = "max ${String.format(Locale.US, "%.1f", maxItem.value).replace('.', ',')}",
                            bgColor = GlucoseDashboardTheme.MaxBadgeColor,
                            modifier = Modifier
                                .onSizeChanged { maxBadgeSize = it }
                                .alpha(if (peakBadgePlacements.max == null) 0f else 1f)
                                .offset { peakBadgePlacements.max?.toIntOffset() ?: IntOffset.Zero }
                        )
                    }

                    // Dynamic Min Peak Badge
                    if (hasDistinctExtremes) minPt?.let { minItem ->
                        if (minPt != maxPt) {
                            PeakBadge(
                                text = "min ${String.format(Locale.US, "%.1f", minItem.value).replace('.', ',')}",
                                bgColor = GlucoseDashboardTheme.MinBadgeColor,
                                modifier = Modifier
                                    .onSizeChanged { minBadgeSize = it }
                                    .alpha(if (peakBadgePlacements.min == null) 0f else 1f)
                                    .offset { peakBadgePlacements.min?.toIntOffset() ?: IntOffset.Zero }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp * designScale))

            // X-Axis Time Labels Row (aligned strictly under the grid)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = (21.5.dp * designScale), end = (4.dp * designScale)),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                displayTimeLabels.forEachIndexed { index, label ->
                    val isLast = index == displayTimeLabels.lastIndex
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = if (isLast) (if (isDarkTheme) Color.White.copy(alpha = 0.4f) else Color(0xFFBBBFCA)) else axisLabelColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

internal data class PeakBadgePlacement(
    val x: Int,
    val y: Int
) {
    fun toIntOffset() = IntOffset(x, y)
}

internal data class PeakBadgePlacements(
    val min: PeakBadgePlacement?,
    val max: PeakBadgePlacement?
)

private fun calculateChartPointOffsets(
    points: List<GlucosePoint>,
    activePeriod: String,
    chartWidth: Float,
    chartHeight: Float,
    maxValue: Float,
    pointRadiusPx: Float,
    rightInsetPx: Float
): List<Offset> {
    if (chartWidth <= 0f || chartHeight <= 0f || maxValue <= 0f) return emptyList()

    val latestPointMinutes = points.maxOfOrNull { it.timeLabel.toMinutes() } ?: return emptyList()
    val periodMinutes = periodToHours(activePeriod) * 60
    val endMinutes = roundUpToHour(latestPointMinutes)
    val startMinutes = endMinutes - periodMinutes
    val usableChartWidth = (chartWidth - rightInsetPx).coerceAtLeast(0f)
    val pointRadius = minOf(pointRadiusPx, chartHeight / 2f)

    return points.map { point ->
        val x = ((point.timeLabel.toMinutes() - startMinutes).toFloat() / periodMinutes)
            .coerceIn(0f, 1f) * usableChartWidth
        val safeValue = point.value.coerceIn(0f, maxValue)
        val y = (chartHeight - (safeValue / maxValue) * chartHeight)
            .coerceIn(pointRadius, maxOf(pointRadius, chartHeight - pointRadius))
        Offset(x, y)
    }
}

/**
 * Positions badges in the measured graph bounds rather than a fixed design frame.
 * The minimum prefers the free space above its point and the maximum prefers the
 * free space below its point; either one flips when its preferred side is unavailable.
 */
internal fun calculatePeakBadgePlacements(
    chartSize: IntSize,
    minPoint: Offset?,
    maxPoint: Offset?,
    minBadgeSize: IntSize,
    maxBadgeSize: IntSize,
    edgePx: Int,
    gapPx: Int
): PeakBadgePlacements {
    if (chartSize == IntSize.Zero) return PeakBadgePlacements(min = null, max = null)

    var minPlacement = minPoint?.takeIf { minBadgeSize != IntSize.Zero }?.let {
        placeBadge(it, minBadgeSize, chartSize, edgePx, gapPx, preferAbove = true)
    }
    var maxPlacement = maxPoint?.takeIf { maxBadgeSize != IntSize.Zero }?.let {
        placeBadge(it, maxBadgeSize, chartSize, edgePx, gapPx, preferAbove = false)
    }

    if (minPlacement != null && maxPlacement != null &&
        placementsIntersect(minPlacement, minBadgeSize, maxPlacement, maxBadgeSize)
    ) {
        val actualMinPoint = minPoint ?: return PeakBadgePlacements(min = null, max = maxPlacement)
        val actualMaxPoint = maxPoint ?: return PeakBadgePlacements(min = minPlacement, max = null)
        val flippedMin = placeBadge(actualMinPoint, minBadgeSize, chartSize, edgePx, gapPx, preferAbove = false)
        val flippedMax = placeBadge(actualMaxPoint, maxBadgeSize, chartSize, edgePx, gapPx, preferAbove = true)

        when {
            !placementsIntersect(flippedMin, minBadgeSize, maxPlacement, maxBadgeSize) -> {
                minPlacement = flippedMin
            }
            !placementsIntersect(minPlacement, minBadgeSize, flippedMax, maxBadgeSize) -> {
                maxPlacement = flippedMax
            }
        }
    }

    return PeakBadgePlacements(min = minPlacement, max = maxPlacement)
}

private fun placeBadge(
    point: Offset,
    badgeSize: IntSize,
    chartSize: IntSize,
    edgePx: Int,
    gapPx: Int,
    preferAbove: Boolean
): PeakBadgePlacement {
    fun candidate(above: Boolean): PeakBadgePlacement = PeakBadgePlacement(
        x = (point.x.roundToInt() - badgeSize.width / 2),
        y = point.y.roundToInt() + if (above) -badgeSize.height - gapPx else gapPx
    )

    val preferred = candidate(preferAbove)
    val alternative = candidate(!preferAbove)
    return when {
        preferred.fitsInside(chartSize, badgeSize, edgePx) -> preferred
        alternative.fitsInside(chartSize, badgeSize, edgePx) -> alternative
        else -> preferred.clampInside(chartSize, badgeSize, edgePx)
    }
}

private fun PeakBadgePlacement.fitsInside(
    chartSize: IntSize,
    badgeSize: IntSize,
    edgePx: Int
): Boolean = x >= edgePx && y >= edgePx &&
    x + badgeSize.width <= chartSize.width - edgePx &&
    y + badgeSize.height <= chartSize.height - edgePx

private fun PeakBadgePlacement.clampInside(
    chartSize: IntSize,
    badgeSize: IntSize,
    edgePx: Int
): PeakBadgePlacement {
    val maxX = (chartSize.width - badgeSize.width - edgePx).coerceAtLeast(edgePx)
    val maxY = (chartSize.height - badgeSize.height - edgePx).coerceAtLeast(edgePx)
    return copy(x = x.coerceIn(edgePx, maxX), y = y.coerceIn(edgePx, maxY))
}

private fun placementsIntersect(
    first: PeakBadgePlacement,
    firstSize: IntSize,
    second: PeakBadgePlacement,
    secondSize: IntSize
): Boolean = first.x < second.x + secondSize.width &&
    first.x + firstSize.width > second.x &&
    first.y < second.y + secondSize.height &&
    first.y + firstSize.height > second.y

private fun filterPointsAndLabelsForPeriod(
    rawPoints: List<GlucosePoint>,
    period: String
): Pair<List<GlucosePoint>, List<String>> {
    if (rawPoints.isEmpty()) {
        return emptyList<GlucosePoint>() to emptyList()
    }

    val hours = when (period.replace(" ", "")) {
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
