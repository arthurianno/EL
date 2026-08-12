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
import java.util.Locale

data class GlucosePoint(
    val timeLabel: String,
    val value: Float
)

@Composable
fun GlucoseLineChartCard(
    isDarkTheme: Boolean = false,
    selectedPeriod: String = "6ч",
    onPeriodSelected: (String) -> Unit = {},
    onChartClick: () -> Unit = {},
    points: List<GlucosePoint> = emptyList()
) {
    var activePeriod by remember { mutableStateOf(selectedPeriod) }
    val periods = listOf("3ч", "6ч", "12ч", "24ч")

    val cardBg = if (isDarkTheme) GlucoseDashboardTheme.DarkCardBackground else GlucoseDashboardTheme.LightCardBackground
    val cardTextColor = if (isDarkTheme) Color.White else Color(0xFF17191F)
    val gridLineColor = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color(0xFFE3E3E3)
    val axisLabelColor = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color(0xFF878B93)

    val (displayPoints, displayTimeLabels) = remember(points, activePeriod) {
        filterPointsAndLabelsForPeriod(points, activePeriod)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(cardBg)
            .padding(16.dp)
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
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { }
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Сегодня",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = cardTextColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_down),
                        contentDescription = "Select Date",
                        tint = cardTextColor,
                        modifier = Modifier.height(20.dp)
                    )
                }

                // Time Filter Segmented Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color(0xFFBBBFCA),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(2.dp)
                ) {
                    periods.forEach { period ->
                        val isSelected = period == activePeriod
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
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
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else axisLabelColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Graph Section with Y-Axis Scale on the Left + Canvas + X-Axis Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Y-Axis Scale Labels (16, 12, 8, 4, 0)
                Column(
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight()
                        .padding(bottom = 18.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    listOf("16", "12", "8", "4", "0").forEach { yVal ->
                        Text(
                            text = yVal,
                            fontSize = 11.sp,
                            color = axisLabelColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

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

                    var maxOffset by remember { mutableStateOf<Offset?>(null) }
                    var minOffset by remember { mutableStateOf<Offset?>(null) }

                    Canvas(modifier = Modifier.matchParentSize()) {
                        val width = size.width
                        val height = size.height
                        val paddingBottom = 20.dp.toPx()
                        val chartWidth = width
                        val chartHeight = height - paddingBottom

                        // Draw Horizontal Grid Lines
                        val yLevels = listOf(16f, 12f, 8f, 4f, 0f)
                        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                        yLevels.forEachIndexed { index, _ ->
                            val y = (index.toFloat() / (yLevels.size - 1)) * chartHeight
                            drawLine(
                                color = gridLineColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                pathEffect = pathEffect,
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Draw Curve Path
                        val maxVal = 16f
                        val path = Path()
                        val linePoints = displayPoints.mapIndexed { index, pt ->
                            val divisor = (displayPoints.size - 1).coerceAtLeast(1)
                            val x = (index.toFloat() / divisor) * chartWidth
                            val y = chartHeight - (pt.value / maxVal) * chartHeight
                            Offset(x, y)
                        }

                        if (linePoints.isNotEmpty()) {
                            path.moveTo(linePoints.first().x, linePoints.first().y)
                            for (i in 0 until linePoints.size - 1) {
                                val p1 = linePoints[i]
                                val p2 = linePoints[i + 1]
                                val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2, p1.y)
                                val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2, p2.y)
                                path.cubicTo(
                                    controlPoint1.x, controlPoint1.y,
                                    controlPoint2.x, controlPoint2.y,
                                    p2.x, p2.y
                                )
                            }

                            val strokeGradient = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFA726), // High glucose level (top): Amber/Orange
                                    Color(0xFF3BB2B8), // Normal glucose level (middle): Green/Teal
                                    Color(0xFFF85F73)  // Low glucose level (bottom): Red
                                )
                            )

                            drawPath(
                                path = path,
                                brush = strokeGradient,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            linePoints.forEachIndexed { idx, point ->
                                val value = displayPoints[idx].value
                                val dotColor = when {
                                    value >= 12f -> GlucoseDashboardTheme.MaxBadgeColor
                                    value <= 3.9f -> GlucoseDashboardTheme.MinBadgeColor
                                    else -> Color(0xFF3BB2B8)
                                }
                                drawCircle(
                                    color = Color.White,
                                    radius = 6.dp.toPx(),
                                    center = point
                                )
                                drawCircle(
                                    color = dotColor,
                                    radius = 4.dp.toPx(),
                                    center = point
                                )
                            }

                            if (maxIdx >= 0 && maxIdx < linePoints.size) {
                                maxOffset = linePoints[maxIdx]
                            }
                            if (minIdx >= 0 && minIdx < linePoints.size && minIdx != maxIdx) {
                                minOffset = linePoints[minIdx]
                            }
                        }
                    }

                    // Dynamic Max Peak Badge
                    maxPt?.let { maxItem ->
                        maxOffset?.let { pt ->
                            val xDp = with(LocalDensity.current) { pt.x.toDp() }
                            val yDp = with(LocalDensity.current) { pt.y.toDp() }
                            PeakBadge(
                                text = "max ${String.format(Locale.US, "%.1f", maxItem.value).replace('.', ',')}",
                                bgColor = GlucoseDashboardTheme.MaxBadgeColor,
                                modifier = Modifier.padding(
                                    start = (xDp - 30.dp).coerceIn(4.dp, 240.dp),
                                    top = (yDp - 26.dp).coerceIn(2.dp, 130.dp)
                                )
                            )
                        }
                    }

                    // Dynamic Min Peak Badge
                    minPt?.let { minItem ->
                        if (minPt != maxPt) {
                            minOffset?.let { pt ->
                                val xDp = with(LocalDensity.current) { pt.x.toDp() }
                                val yDp = with(LocalDensity.current) { pt.y.toDp() }
                                PeakBadge(
                                    text = "min ${String.format(Locale.US, "%.1f", minItem.value).replace('.', ',')}",
                                    bgColor = GlucoseDashboardTheme.MinBadgeColor,
                                    modifier = Modifier.padding(
                                        start = (xDp - 30.dp).coerceIn(4.dp, 240.dp),
                                        top = (yDp + 8.dp).coerceIn(2.dp, 145.dp)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // X-Axis Time Labels Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                displayTimeLabels.forEach { label ->
                    Text(
                        text = label,
                        fontSize = 11.sp,
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
    val sample3h = listOf(
        GlucosePoint("11:00", 7.8f),
        GlucosePoint("11:30", 11.2f),
        GlucosePoint("12:00", 9.4f),
        GlucosePoint("12:45", 15.8f),
        GlucosePoint("13:30", 5.4f),
        GlucosePoint("14:00", 3.2f)
    )
    val sample3hLabels = listOf("11:00", "11:30", "12:00", "12:30", "13:00", "14:00")

    val sample6h = listOf(
        GlucosePoint("09:00", 4.2f),
        GlucosePoint("09:30", 3.2f),
        GlucosePoint("10:30", 14.5f),
        GlucosePoint("11:45", 7.8f),
        GlucosePoint("12:45", 15.8f),
        GlucosePoint("13:30", 3.8f),
        GlucosePoint("14:00", 1.7f)
    )
    val sample6hLabels = listOf("09:00", "10:00", "11:00", "12:00", "13:00", "14:00")

    val sample12h = listOf(
        GlucosePoint("02:00", 6.1f),
        GlucosePoint("05:00", 5.4f),
        GlucosePoint("07:30", 8.2f),
        GlucosePoint("09:30", 3.2f),
        GlucosePoint("10:30", 14.5f),
        GlucosePoint("12:45", 15.8f),
        GlucosePoint("14:00", 1.7f)
    )
    val sample12hLabels = listOf("02:00", "04:00", "06:00", "08:00", "10:00", "12:00", "14:00")

    val sample24h = listOf(
        GlucosePoint("00:00", 5.8f),
        GlucosePoint("04:00", 6.2f),
        GlucosePoint("08:00", 5.2f),
        GlucosePoint("10:30", 14.5f),
        GlucosePoint("12:45", 15.8f),
        GlucosePoint("16:00", 6.5f),
        GlucosePoint("20:00", 5.1f),
        GlucosePoint("24:00", 4.8f)
    )
    val sample24hLabels = listOf("00:00", "04:00", "08:00", "12:00", "16:00", "20:00", "24:00")

    if (rawPoints.isEmpty()) {
        return when (period) {
            "3ч" -> Pair(sample3h, sample3hLabels)
            "6ч" -> Pair(sample6h, sample6hLabels)
            "12ч" -> Pair(sample12h, sample12hLabels)
            "24ч" -> Pair(sample24h, sample24hLabels)
            else -> Pair(sample6h, sample6hLabels)
        }
    }

    val hours = when (period) {
        "3ч" -> 3
        "6ч" -> 6
        "12ч" -> 12
        "24ч" -> 24
        else -> 6
    }

    val countNeeded = when (hours) {
        3 -> 4
        6 -> 6
        12 -> 8
        else -> rawPoints.size
    }

    val filtered = if (rawPoints.size > countNeeded) {
        rawPoints.takeLast(countNeeded)
    } else {
        rawPoints
    }

    val labels = when (hours) {
        3 -> sample3hLabels
        6 -> sample6hLabels
        12 -> sample12hLabels
        24 -> sample24hLabels
        else -> sample6hLabels
    }

    return Pair(filtered, labels)
}

@Composable
private fun PeakBadge(
    text: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
