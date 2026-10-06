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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import org.threeten.bp.LocalTime
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import java.util.Locale

data class GlucosePoint(
    val timeLabel: String,
    val value: Float
)

private const val CHART_MAX_GLUCOSE_VALUE = 40f

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlucoseLineChartCard(
    isDarkTheme: Boolean = false,
    selectedPeriod: String = "6ч",
    onPeriodSelected: (String) -> Unit = {},
    onChartClick: () -> Unit = {},
    points: List<GlucosePoint> = emptyList(),
    selectedDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit = {},
    designScale: Float = 1f,
    cardHeight: androidx.compose.ui.unit.Dp = 201.dp * designScale,
    emptyStateText: String = "Нет измерений за выбранный период",
    showDetailHint: Boolean = false
) {
    val fontScale = LocalDensity.current.fontScale
    val largeText = fontScale > 1.3f
    var activePeriod by remember { mutableStateOf(selectedPeriod) }
    var isDatePickerVisible by remember { mutableStateOf(false) }
    val periods = listOf("3 ч", "6 ч", "12 ч", "24 ч")

    val cardBg = if (isDarkTheme) GlucoseDashboardTheme.DarkCardBackground else GlucoseDashboardTheme.LightCardBackground
    val cardTextColor = if (isDarkTheme) Color.White else Color(0xFF17191F)
    val gridLineColor = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color(0xFFE3E3E3)
    val axisLabelColor = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color(0xFF878B93)

    fun handleChartClick() = onChartClick()

    val (filteredPoints, filteredTimeLabels) = remember(points, activePeriod, selectedDate) {
        filterPointsAndLabelsForPeriod(points, activePeriod, selectedDate)
    }
    val displayPoints = filteredPoints
    val displayTimeLabels = if (largeText && filteredTimeLabels.size > 2)
        listOf(filteredTimeLabels.first(), filteredTimeLabels.last()) else filteredTimeLabels

    val maxPointVal = displayPoints.maxOfOrNull { it.value } ?: 0f
    val maxVal = if (maxPointVal > 16f) 20f else 16f
    val yLabels = if (maxVal == 20f) listOf("20", "15", "10", "5", "0") else listOf("16", "12", "8", "4", "0")

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(
                    start = ChartCardHorizontalInset * designScale,
                    top = ChartCardTopInset * designScale,
                    end = ChartCardHorizontalInset * designScale
                )
                .fillMaxWidth()
                .height(if (largeText) (cardHeight * fontScale).coerceAtLeast(380.dp) else cardHeight)
                .clip(RoundedCornerShape(13.dp * designScale))
                .border(
                    width = 1.dp,
                    color = if (isDarkTheme) GlucoseDashboardTheme.DarkCardBorder else Color(0xFFE3E3E3),
                    shape = RoundedCornerShape(13.dp * designScale)
                )
                .background(cardBg)
                .padding(
                    start = 8.dp * designScale,
                    top = 4.dp * designScale,
                    end = 12.dp * designScale,
                    bottom = 10.dp * designScale
                )
        ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Wrap the period selector under the date when either text or screen width needs it.
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp * designScale),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Date Picker Dropdown Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp * designScale))
                        .clickable { isDatePickerVisible = true }
                        .heightIn(min = 40.dp * designScale)
                        .padding(vertical = 2.dp * designScale, horizontal = 4.dp * designScale),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedDate == LocalDate.now()) "Сегодня" else
                            String.format(Locale.US, "%02d.%02d.%04d", selectedDate.dayOfMonth, selectedDate.monthValue, selectedDate.year),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cardTextColor
                    )
                    Spacer(modifier = Modifier.width(4.dp * designScale))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_down),
                        contentDescription = null,
                        tint = cardTextColor,
                        modifier = Modifier.size(14.dp * designScale)
                    )
                }

                // Time Filter Segmented Switcher (149x24 dp in Figma reference)
                Row(
                    modifier = Modifier
                        .heightIn(min = 24.dp * designScale)
                        .horizontalScroll(rememberScrollState())
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
                                .heightIn(min = 20.dp * designScale)
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
                        .width((if (largeText) 24.dp else 16.dp) * designScale * fontScale)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    yLabels.forEach { yVal ->
                        Text(
                            text = yVal,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 1,
                            softWrap = false,
                            color = axisLabelColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(5.5.dp * designScale))

                // Chart canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { handleChartClick() }
                ) {
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
                            text = emptyStateText,
                            fontSize = 13.sp,
                            color = axisLabelColor,
                            modifier = Modifier.align(Alignment.Center)
                        )
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

        if (showDetailHint) {
            Spacer(modifier = Modifier.height(ChartDetailHintTopInset * designScale))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = ChartDetailHintHeight * designScale)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_info_circle),
                    contentDescription = null,
                    tint = axisLabelColor,
                    modifier = Modifier.size(18.dp * designScale)
                )
                Spacer(modifier = Modifier.width(8.dp * designScale))
                Text(
                    text = "Нажмите на график для большей статистики",
                    fontSize = 11.sp,
                    color = axisLabelColor
                )
            }
        }

        Spacer(modifier = Modifier.height(ChartCardBottomInset * designScale))
    }
    if (isDatePickerVisible) {
        GlucoseDatePickerDialog(
            initialDate = selectedDate,
            minDate = YearMonth.from(LocalDate.now()).minusMonths(11).atDay(1),
            onDismissRequest = { isDatePickerVisible = false },
            onDateRangeSelected = { date, _ ->
                onDateSelected(date)
                isDatePickerVisible = false
            }
        )
    }
}

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

private fun filterPointsAndLabelsForPeriod(
    rawPoints: List<GlucosePoint>,
    period: String,
    selectedDate: LocalDate
): Pair<List<GlucosePoint>, List<String>> {
    if (rawPoints.isEmpty()) {
        val hours = periodToHours(period)
        // The empty graph still represents the selected time window. Keeping these labels
        // visible provides the same context as a populated chart instead of leaving its
        // horizontal axis blank.
        val now = LocalTime.now()
        val currentHourMinutes = if (selectedDate == LocalDate.now()) now.hour * 60 else 24 * 60
        return emptyList<GlucosePoint>() to buildTimelineLabels(
            endMinutes = currentHourMinutes,
            hours = hours,
            period = period
        )
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
