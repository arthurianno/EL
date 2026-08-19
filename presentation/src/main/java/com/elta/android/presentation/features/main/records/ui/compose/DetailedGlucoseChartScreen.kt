package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R

data class DetailedGlucosePoint(
    val timeLabel: String,
    val value: Float,
    val isMin: Boolean = false,
    val isMax: Boolean = false,
    val trendText: String = "стабилен",
    val trendValue: String = "0,0",
    val foodTimeAgo: String? = null,
    val foodUnits: String? = null,
    val insulinTimeAgo: String? = null,
    val insulinUnits: String? = null,
    val activityTimeAgo: String? = null,
    val activityDuration: String? = null
)

data class DetailedInsulinEntry(
    val timeLabel: String,
    val xIndex: Int,
    val units: String,
    val heightRatio: Float
)

data class DetailedFoodEntry(
    val timeLabel: String,
    val xIndex: Int,
    val breadUnits: String,
    val heightRatio: Float
)

data class DetailedActivityEntry(
    val startTimeLabel: String,
    val endTimeLabel: String,
    val durationMins: Long
)

private val DetailedChartBackground = Color(0xFF1FBFD2)
private val DetailedChartCardBorder = Color(0xFFA4A4A4)
private val DetailedChartTextPrimary = Color(0xFF3D4556)
private val DetailedChartTextSecondary = Color(0xFF878B93)
private val DetailedLowColor = Color(0xFFD93B17)
private val DetailedNormalColor = Color(0xFF29AF99)
private val DetailedHighColor = Color(0xFFEE9C17)
private const val DETAILED_GRAPH_START_MINUTES = 2 * 60
private const val DETAILED_GRAPH_END_MINUTES = 14 * 60

fun getTodayFormattedDate(): String {
    val now = org.threeten.bp.LocalDate.now()
    val months = listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    )
    return "${now.dayOfMonth} ${months[now.monthValue - 1]} ${now.year}"
}

fun getTimeOfDayFraction(timeLabel: String): Float {
    return try {
        val parts = timeLabel.trim().split(":")
        if (parts.size >= 2) {
            val hours = parts[0].toFloatOrNull() ?: 0f
            val minutes = parts[1].toFloatOrNull() ?: 0f
            val totalMins = hours * 60f + minutes
            (totalMins / 1440f).coerceIn(0f, 1f)
        } else {
            0.5f
        }
    } catch (e: Exception) {
        0.5f
    }
}

@Composable
fun DetailedGlucoseChartScreen(
    onBackClick: () -> Unit = {},
    initialDate: String = getTodayFormattedDate(),
    glucosePoints: List<DetailedGlucosePoint> = emptyList(),
    insulinEntries: List<DetailedInsulinEntry> = emptyList(),
    foodEntries: List<DetailedFoodEntry> = emptyList(),
    activityEntries: List<DetailedActivityEntry> = emptyList()
) {
    val activity = LocalContext.current.findActivity()
    DisposableEffect(activity) {
        val previousOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            if (previousOrientation != null) {
                activity.requestedOrientation = previousOrientation
            }
        }
    }

    var selectedDate by rememberSaveable { mutableStateOf(initialDate) }
    var isDatePickerVisible by remember { mutableStateOf(false) }

    // Layer toggles
    var isInsulinLayerVisible by remember { mutableStateOf(true) }
    var isFoodLayerVisible by remember { mutableStateOf(true) }
    var isActivityLayerVisible by remember { mutableStateOf(true) }

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    val totalPoints = glucosePoints.size
    val hasData = totalPoints >= 2
    val averageValue = if (hasData) glucosePoints.map { it.value }.average().toFloat() else 0f
    val averageValueText = if (hasData) {
        String.format(java.util.Locale.US, "%.1f", averageValue).replace('.', ',')
    } else {
        "-"
    }
    val normalCount = glucosePoints.count { it.value in 3.91f..9.99f }
    val highCount = glucosePoints.count { it.value >= 10.0f }
    val lowCount = glucosePoints.count { it.value <= 3.9f }
    val normalPercent = if (totalPoints > 0) normalCount * 100 / totalPoints else 0
    val highPercent = if (totalPoints > 0) highCount * 100 / totalPoints else 0
    val lowPercent = if (totalPoints > 0) lowCount * 100 / totalPoints else 0
    val sdValue = if (hasData) {
        val variance = glucosePoints.map { point ->
            val diff = point.value - averageValue
            diff * diff
        }.average()
        Math.sqrt(variance).toFloat()
    } else {
        0f
    }
    val cvText = if (hasData && averageValue > 0f) "${Math.round(sdValue / averageValue * 100)}%" else "-"
    val sdText = if (hasData) String.format(java.util.Locale.US, "%.1f", sdValue).replace('.', ',') else "-"
    val gmiText = if (hasData) {
        val gmi = 12.71f + 0.091f * (averageValue * 18.0182f)
        "${String.format(java.util.Locale.US, "%.1f", gmi).replace('.', ',')}%"
    } else {
        "-"
    }

    Dialog(
        onDismissRequest = onBackClick,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val view = LocalView.current
        SideEffect {
            val window = (view.parent as? DialogWindowProvider)?.window
            window?.let { w ->
                w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                w.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(DetailedChartBackground)
        ) {
            val contentWidth = maxWidth.coerceAtMost(737.dp)
            val isVeryShort = maxHeight < 360.dp
            val chartCardHeight = if (isVeryShort) 220.dp else 234.dp
            val bottomCardHeight = if (isVeryShort) 74.dp else 82.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(top = if (isVeryShort) 12.dp else 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .width(contentWidth)
                        .height(22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onBackClick() }
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_left),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Назад",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(7.dp))

                Box(
                    modifier = Modifier
                        .width(contentWidth)
                        .height(chartCardHeight)
                        .clip(RoundedCornerShape(13.dp))
                        .border(1.dp, DetailedChartCardBorder, RoundedCornerShape(13.dp))
                        .background(Color.White)
                        .padding(start = 20.dp, top = 16.dp, end = 12.dp, bottom = 10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { isDatePickerVisible = true }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedDate,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DetailedChartTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_arrow_left),
                                        contentDescription = "Select Date",
                                        tint = DetailedChartTextPrimary,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .rotate(270f)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Количество измерений:",
                                        fontSize = 12.sp,
                                        color = DetailedChartTextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = totalPoints.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DetailedChartTextPrimary.copy(alpha = 0.9f)
                                    )
                                    Spacer(modifier = Modifier.width(24.dp))
                                    LegendDotItem(color = DetailedLowColor, label = "Низкий")
                                    Spacer(modifier = Modifier.width(16.dp))
                                    LegendDotItem(color = DetailedNormalColor, label = "Норма")
                                    Spacer(modifier = Modifier.width(16.dp))
                                    LegendDotItem(color = DetailedHighColor, label = "Высокий")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .fillMaxHeight()
                                        .padding(bottom = 20.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    listOf("16", "12", "8", "4", "0").forEach { yValue ->
                                        Text(
                                            text = yValue,
                                            fontSize = 12.sp,
                                            color = DetailedChartTextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                BoxWithConstraints(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    val graphWidth = maxWidth
                                    val maxPoint = glucosePoints.maxByOrNull { it.value }
                                    val minPoint = glucosePoints.minByOrNull { it.value }
                                    val maxPointIndex = maxPoint?.let { glucosePoints.indexOf(it) } ?: -1
                                    val minPointIndex = minPoint?.let { glucosePoints.indexOf(it) } ?: -1

                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .pointerInput(glucosePoints) {
                                                detectTapGestures { offset ->
                                                    if (glucosePoints.isNotEmpty()) {
                                                        selectedPointIndex = glucosePoints.indices.minByOrNull { index ->
                                                            val pointX = detailedGraphFraction(glucosePoints[index].timeLabel) * size.width
                                                            kotlin.math.abs(pointX - offset.x)
                                                        }
                                                    }
                                                }
                                            }
                                    ) {
                                        val chartHeight = size.height - 20.dp.toPx()
                                        val dash = PathEffect.dashPathEffect(floatArrayOf(7f, 7f), 0f)
                                        listOf(16f, 12f, 8f, 4f, 0f).forEachIndexed { index, _ ->
                                            val y = index.toFloat() / 4f * chartHeight
                                            drawLine(
                                                color = Color(0xFFE1E4E8),
                                                start = Offset(0f, y),
                                                end = Offset(size.width, y),
                                                pathEffect = dash,
                                                strokeWidth = 1.dp.toPx()
                                            )
                                        }

                                        if (isFoodLayerVisible) {
                                            foodEntries.forEach { food ->
                                                val x = detailedGraphFraction(food.timeLabel) * size.width
                                                val barWidth = 18.dp.toPx()
                                                val barHeight = chartHeight * food.heightRatio
                                                drawRoundRect(
                                                    color = Color(0xFFFF8058).copy(alpha = 0.18f),
                                                    topLeft = Offset(x - barWidth / 2f, chartHeight - barHeight),
                                                    size = Size(barWidth, barHeight),
                                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                                )
                                            }
                                        }

                                        if (isInsulinLayerVisible) {
                                            insulinEntries.forEach { insulin ->
                                                val x = detailedGraphFraction(insulin.timeLabel) * size.width
                                                val barWidth = 18.dp.toPx()
                                                val barHeight = chartHeight * insulin.heightRatio
                                                drawRoundRect(
                                                    color = Color(0xFF38B7E1).copy(alpha = 0.18f),
                                                    topLeft = Offset(x - barWidth / 2f, chartHeight - barHeight),
                                                    size = Size(barWidth, barHeight),
                                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                                )
                                            }
                                        }

                                        val linePoints = glucosePoints.map { point ->
                                            Offset(
                                                x = detailedGraphFraction(point.timeLabel) * size.width,
                                                y = chartHeight - (point.value / 16f).coerceIn(0f, 1f) * chartHeight
                                            )
                                        }

                                        if (linePoints.size > 1) {
                                            for (index in 0 until linePoints.lastIndex) {
                                                val start = linePoints[index]
                                                val end = linePoints[index + 1]
                                                val value = glucosePoints[index].value
                                                val path = Path().apply {
                                                    moveTo(start.x, start.y)
                                                    cubicTo(
                                                        start.x + (end.x - start.x) / 2f,
                                                        start.y,
                                                        start.x + (end.x - start.x) / 2f,
                                                        end.y,
                                                        end.x,
                                                        end.y
                                                    )
                                                }
                                                drawPath(
                                                    path = path,
                                                    color = detailedPointColor(value),
                                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                                )
                                            }
                                        }

                                        linePoints.forEachIndexed { index, point ->
                                            val isSelected = selectedPointIndex == index
                                            drawCircle(
                                                color = Color.White,
                                                radius = if (isSelected) 8.dp.toPx() else 4.2.dp.toPx(),
                                                center = point
                                            )
                                            drawCircle(
                                                color = detailedPointColor(glucosePoints[index].value),
                                                radius = if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(),
                                                center = point
                                            )
                                        }

                                        selectedPointIndex?.let { index ->
                                            linePoints.getOrNull(index)?.let { point ->
                                                drawLine(
                                                    color = DetailedChartTextSecondary.copy(alpha = 0.7f),
                                                    start = Offset(point.x, 0f),
                                                    end = Offset(point.x, chartHeight),
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f),
                                                    strokeWidth = 1.dp.toPx()
                                                )
                                                drawCircle(
                                                    color = DetailedChartTextPrimary,
                                                    radius = 7.dp.toPx(),
                                                    center = point,
                                                    style = Stroke(width = 1.5.dp.toPx())
                                                )
                                            }
                                        }

                                        if (isActivityLayerVisible) {
                                            activityEntries.forEach { activityEntry ->
                                                val start = detailedGraphFraction(activityEntry.startTimeLabel) * size.width
                                                val end = detailedGraphFraction(activityEntry.endTimeLabel) * size.width
                                                drawLine(
                                                    color = Color(0xFF6078EA),
                                                    start = Offset(start, chartHeight + 4.dp.toPx()),
                                                    end = Offset(end.coerceAtLeast(start + 18.dp.toPx()), chartHeight + 4.dp.toPx()),
                                                    strokeWidth = 3.dp.toPx(),
                                                    cap = StrokeCap.Round
                                                )
                                            }
                                        }
                                    }

                                    if (glucosePoints.isEmpty()) {
                                        Text(
                                            text = "Нет измерений за выбранный день",
                                            fontSize = 13.sp,
                                            color = DetailedChartTextSecondary,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }

                                    if (maxPointIndex >= 0) {
                                        val fraction = detailedGraphFraction(glucosePoints[maxPointIndex].timeLabel)
                                        PeakBadgeOverlay(
                                            text = "max ${String.format(java.util.Locale.US, "%.1f", glucosePoints[maxPointIndex].value).replace('.', ',')}",
                                            bgColor = DetailedHighColor,
                                            modifier = Modifier.padding(
                                                start = (graphWidth * fraction - 34.dp).coerceIn(0.dp, (graphWidth - 70.dp).coerceAtLeast(0.dp)),
                                                top = 8.dp
                                            )
                                        )
                                    }

                                    if (minPointIndex >= 0 && minPointIndex != maxPointIndex) {
                                        val fraction = detailedGraphFraction(glucosePoints[minPointIndex].timeLabel)
                                        PeakBadgeOverlay(
                                            text = "min ${String.format(java.util.Locale.US, "%.1f", glucosePoints[minPointIndex].value).replace('.', ',')}",
                                            bgColor = DetailedLowColor,
                                            modifier = Modifier.padding(
                                                start = (graphWidth * fraction - 34.dp).coerceIn(0.dp, (graphWidth - 70.dp).coerceAtLeast(0.dp)),
                                                top = 112.dp
                                            )
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 22.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00").forEach { label ->
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        color = DetailedChartTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 22.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "17.08.2025",
                                    fontSize = 11.sp,
                                    color = DetailedChartTextSecondary,
                                    modifier = Modifier.width(84.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFD9D9D9).copy(alpha = 0.57f))
                                        .border(1.dp, DetailedChartTextSecondary.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                )
                                Text(
                                    text = "17.08.2026",
                                    fontSize = 11.sp,
                                    color = DetailedChartTextSecondary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(84.dp)
                                )
                            }
                        }

                        Column {
                            Spacer(modifier = Modifier.height(50.dp))
                            LayerToggleButton(
                                iconRes = R.drawable.ic_save_edit,
                                isActive = isInsulinLayerVisible,
                                activeBgColor = Color(0xFFE0F6FF),
                                activeTint = Color(0xFF38B7E1),
                                onClick = { isInsulinLayerVisible = !isInsulinLayerVisible }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LayerToggleButton(
                                iconRes = R.drawable.ic_verify_dish,
                                isActive = isFoodLayerVisible,
                                activeBgColor = Color(0xFFFFE7DF),
                                activeTint = Color(0xFFFF8058),
                                onClick = { isFoodLayerVisible = !isFoodLayerVisible }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LayerToggleButton(
                                iconRes = R.drawable.ic_list,
                                isActive = isActivityLayerVisible,
                                activeBgColor = Color(0xFFE7EAFF),
                                activeTint = Color(0xFF6078EA),
                                onClick = { isActivityLayerVisible = !isActivityLayerVisible }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .width(contentWidth)
                        .height(bottomCardHeight)
                        .clip(RoundedCornerShape(13.dp))
                        .border(1.dp, DetailedChartCardBorder, RoundedCornerShape(13.dp))
                        .background(Color.White)
                        .padding(horizontal = 30.dp, vertical = if (isVeryShort) 10.dp else 14.dp)
                ) {
                    val selectedPoint = selectedPointIndex?.let { glucosePoints.getOrNull(it) }

                    if (selectedPoint != null) {
                        SelectedPointSummaryRow(selectedPoint)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.width(98.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "События дня",
                                    fontSize = 12.sp,
                                    color = DetailedChartTextSecondary
                                )
                                Text(
                                    text = selectedDate,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DetailedChartTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }

                            DetailedVerticalDivider()

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = averageValueText,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DetailedHighColor,
                                    letterSpacing = 0.sp
                                )
                                Text(
                                    text = "ммоль/л",
                                    fontSize = 12.sp,
                                    color = DetailedHighColor,
                                    lineHeight = 12.sp
                                )
                                Text(
                                    text = "средний за день",
                                    fontSize = 10.sp,
                                    color = DetailedChartTextSecondary,
                                    lineHeight = 12.sp
                                )
                            }

                            DetailedVerticalDivider()

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TirStatItem(color = DetailedNormalColor, percent = if (hasData) "$normalPercent%" else "-", label = if (hasData) percentToDuration(normalPercent) else "-")
                                Spacer(modifier = Modifier.width(14.dp))
                                TirStatItem(color = DetailedHighColor, percent = if (hasData) "$highPercent%" else "-", label = if (hasData) percentToDuration(highPercent) else "-")
                                Spacer(modifier = Modifier.width(14.dp))
                                TirStatItem(color = DetailedLowColor, percent = if (hasData) "$lowPercent%" else "-", label = if (hasData) percentToDuration(lowPercent) else "-")
                            }

                            DetailedVerticalDivider()

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IndicatorStatItem(name = "CV", value = cvText)
                                Spacer(modifier = Modifier.width(18.dp))
                                IndicatorStatItem(name = "SD", value = sdText)
                                Spacer(modifier = Modifier.width(18.dp))
                                IndicatorStatItem(name = "GMI", value = gmiText)
                            }
                        }
                    }
                }
            }
        }

        if (isDatePickerVisible) {
            GlucoseDatePickerDialog(
                initialDate = selectedDate,
                onDismissRequest = { isDatePickerVisible = false },
                onDateSelected = { date ->
                    selectedDate = date
                    isDatePickerVisible = false
                }
            )
        }
    }
}

private fun detailedGraphFraction(timeLabel: String): Float {
    val minutes = timeLabelToMinutes(timeLabel)
    return ((minutes - DETAILED_GRAPH_START_MINUTES).toFloat() /
        (DETAILED_GRAPH_END_MINUTES - DETAILED_GRAPH_START_MINUTES)).coerceIn(0f, 1f)
}

private fun timeLabelToMinutes(timeLabel: String): Int {
    val parts = timeLabel.trim().split(":")
    val hours = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return (hours * 60 + minutes).coerceIn(0, 24 * 60)
}

private fun detailedPointColor(value: Float): Color = when {
    value >= 10f -> DetailedHighColor
    value <= 3.9f -> DetailedLowColor
    else -> DetailedNormalColor
}

private fun percentToDuration(percent: Int): String {
    val totalMinutes = percent * 24 * 60 / 100
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}ч ${minutes}м"
        hours > 0 -> "${hours}ч"
        else -> "${minutes}м"
    }
}

@Composable
private fun SelectedPointSummaryRow(point: DetailedGlucosePoint) {
    val glucoseColor = detailedPointColor(point.value)
    val trendIcon = when {
        point.trendValue.startsWith("+") -> "↗"
        point.trendValue.startsWith("-") -> "↘"
        else -> "→"
    }
    val trendColor = when {
        point.trendValue.startsWith("+") -> DetailedHighColor
        point.trendValue.startsWith("-") -> DetailedLowColor
        else -> DetailedNormalColor
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = point.timeLabel,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = DetailedChartTextPrimary,
            modifier = Modifier.width(98.dp),
            textAlign = TextAlign.Center
        )

        DetailedVerticalDivider()

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(java.util.Locale.US, "%.1f", point.value).replace('.', ','),
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
                color = glucoseColor,
                letterSpacing = 0.sp
            )
            Text(
                text = "ммоль/л",
                fontSize = 12.sp,
                color = glucoseColor,
                lineHeight = 12.sp
            )
        }

        DetailedVerticalDivider()

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Тренд",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DetailedChartTextPrimary
            )
            Text(
                text = "$trendIcon ${point.trendText}",
                fontSize = 12.sp,
                color = DetailedChartTextPrimary,
                lineHeight = 14.sp
            )
            Text(
                text = point.trendValue,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = trendColor,
                lineHeight = 18.sp
            )
        }

        DetailedVerticalDivider()
        EventInfoItem(
            iconRes = R.drawable.ic_verify_dish,
            activeColor = Color(0xFFFF8058),
            timeAgo = point.foodTimeAgo,
            value = point.foodUnits
        )
        DetailedVerticalDivider()
        EventInfoItem(
            iconRes = R.drawable.ic_save_edit,
            activeColor = Color(0xFF38B7E1),
            timeAgo = point.insulinTimeAgo,
            value = point.insulinUnits
        )
        DetailedVerticalDivider()
        EventInfoItem(
            iconRes = R.drawable.ic_list,
            activeColor = Color(0xFF6078EA),
            timeAgo = point.activityTimeAgo,
            value = point.activityDuration
        )
    }
}

@Composable
private fun EventInfoItem(
    iconRes: Int,
    activeColor: Color,
    timeAgo: String?,
    value: String?
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = if (value != null) activeColor else Color(0xFFBBBFCA),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = timeAgo ?: "-",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DetailedChartTextPrimary,
                lineHeight = 13.sp
            )
            Text(
                text = if (timeAgo != null) "назад" else "",
                fontSize = 10.sp,
                color = DetailedChartTextPrimary,
                lineHeight = 12.sp
            )
            Text(
                text = value ?: "-",
                fontSize = 11.sp,
                color = DetailedChartTextPrimary,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun DetailedVerticalDivider() {
    Box(
        modifier = Modifier
            .height(48.dp)
            .width(1.dp)
            .background(Color(0xFFE1E4E8))
    )
}

@Composable
private fun LegendDotItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = DetailedChartTextSecondary
        )
    }
}

@Composable
private fun PeakBadgeOverlay(
    text: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
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

@Composable
private fun LayerToggleButton(
    iconRes: Int,
    isActive: Boolean,
    activeBgColor: Color,
    activeTint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isActive) activeBgColor else Color(0xFFF3F4F6))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = "Toggle Layer",
            tint = if (isActive) activeTint else Color(0xFFB0B3BA),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun TirStatItem(color: Color, percent: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "TIR",
                fontSize = 11.sp,
                color = DetailedChartTextPrimary
            )
        }
        Text(
            text = percent,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DetailedChartTextPrimary.copy(alpha = 0.9f),
            lineHeight = 20.sp
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = DetailedChartTextSecondary,
            lineHeight = 12.sp
        )
    }
}

@Composable
private fun IndicatorStatItem(name: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            fontSize = 12.sp,
            color = DetailedChartTextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DetailedChartTextPrimary.copy(alpha = 0.9f),
            lineHeight = 20.sp
        )
    }
}

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

fun defaultDetailedGlucosePoints(): List<DetailedGlucosePoint> = listOf(
    DetailedGlucosePoint("02:00", 3.2f, trendText = "снижается", trendValue = "-0,4", insulinTimeAgo = "3ч назад", insulinUnits = "4,0 Ед."),
    DetailedGlucosePoint("02:30", 1.7f, isMin = true, trendText = "быстро снижается", trendValue = "-1,5", insulinTimeAgo = "3ч 30м назад", insulinUnits = "4,0 Ед."),
    DetailedGlucosePoint("03:00", 4.2f, trendText = "растёт", trendValue = "+2,5", foodTimeAgo = "15м назад", foodUnits = "6,5 ХЕ"),
    DetailedGlucosePoint("03:30", 8.8f, trendText = "быстро растёт", trendValue = "+4,6", foodTimeAgo = "45м назад", foodUnits = "6,5 ХЕ"),
    DetailedGlucosePoint("04:00", 7.2f, trendText = "снижается", trendValue = "-1,6", foodTimeAgo = "1ч 15м назад", foodUnits = "6,5 ХЕ"),
    DetailedGlucosePoint("04:30", 15.8f, isMax = true, trendText = "быстро растёт", trendValue = "+8,6", foodTimeAgo = "1ч 45м назад", foodUnits = "3 ХЕ", insulinTimeAgo = "10м назад", insulinUnits = "15,5 Ед."),
    DetailedGlucosePoint("05:00", 7.9f, trendText = "быстро снижается", trendValue = "-7,9", foodTimeAgo = "2ч 15м назад", foodUnits = "3 ХЕ", insulinTimeAgo = "40м назад", insulinUnits = "15,5 Ед."),
    DetailedGlucosePoint("05:30", 3.8f, trendText = "снижается", trendValue = "-4,1"),
    DetailedGlucosePoint("06:00", 5.2f, trendText = "растёт", trendValue = "+1,4", insulinTimeAgo = "5м назад", insulinUnits = "4,1 Ед."),
    DetailedGlucosePoint("06:30", 3.0f, trendText = "снижается", trendValue = "-2,2"),
    DetailedGlucosePoint("07:00", 2.1f, trendText = "снижается", trendValue = "-0,9"),
    DetailedGlucosePoint("07:30", 6.9f, trendText = "быстро растёт", trendValue = "+4,8", foodTimeAgo = "10м назад", foodUnits = "14 ХЕ"),
    DetailedGlucosePoint("08:00", 3.6f, trendText = "снижается", trendValue = "-3,3", foodTimeAgo = "40м назад", foodUnits = "14 ХЕ"),
    DetailedGlucosePoint("08:05", 4.1f, trendText = "снижается", trendValue = "-0,8", foodTimeAgo = "45м назад", foodUnits = "3 ХЕ", insulinTimeAgo = "1ч 15м назад", insulinUnits = "15,5 Ед.", activityTimeAgo = "7ч 24м назад", activityDuration = "20 мин."),
    DetailedGlucosePoint("08:30", 7.5f, trendText = "растёт", trendValue = "+3,9"),
    DetailedGlucosePoint("09:00", 14.8f, trendText = "быстро растёт", trendValue = "+7,3", foodTimeAgo = "1ч 40м назад", foodUnits = "14 ХЕ", insulinTimeAgo = "5м назад", insulinUnits = "7,3 Ед."),
    DetailedGlucosePoint("09:30", 2.5f, trendText = "быстро снижается", trendValue = "-12,3"),
    DetailedGlucosePoint("10:00", 3.0f, trendText = "растёт", trendValue = "+0,5"),
    DetailedGlucosePoint("10:30", 8.1f, trendText = "быстро растёт", trendValue = "+5,1"),
    DetailedGlucosePoint("11:00", 4.3f, trendText = "снижается", trendValue = "-3,8", activityTimeAgo = "10м назад", activityDuration = "45 мин."),
    DetailedGlucosePoint("11:30", 1.8f, trendText = "быстро снижается", trendValue = "-2,5", activityTimeAgo = "40м назад", activityDuration = "45 мин."),
    DetailedGlucosePoint("12:00", 4.0f, trendText = "растёт", trendValue = "+2,2", foodTimeAgo = "10м назад", foodUnits = "2 ХЕ", activityTimeAgo = "1ч 10м назад", activityDuration = "45 мин."),
    DetailedGlucosePoint("12:30", 5.8f, trendText = "растёт", trendValue = "+1,8", activityTimeAgo = "1ч 40м назад", activityDuration = "45 мин."),
    DetailedGlucosePoint("13:00", 3.1f, trendText = "снижается", trendValue = "-2,7"),
    DetailedGlucosePoint("13:30", 7.2f, trendText = "быстро растёт", trendValue = "+4,1"),
    DetailedGlucosePoint("14:00", 13.5f, trendText = "быстро растёт", trendValue = "+6,3"),
    DetailedGlucosePoint("14:30", 2.0f, trendText = "быстро снижается", trendValue = "-11,5")
)

fun defaultDetailedInsulinEntries(): List<DetailedInsulinEntry> = listOf(
    DetailedInsulinEntry("04:30", 5, "15,5 Ед.", 0.85f),
    DetailedInsulinEntry("06:00", 8, "4,1 Ед.", 0.30f),
    DetailedInsulinEntry("09:00", 14, "7,3 Ед.", 0.50f)
)

fun defaultDetailedFoodEntries(): List<DetailedFoodEntry> = listOf(
    DetailedFoodEntry("03:00", 2, "6,5 ХЕ", 0.55f),
    DetailedFoodEntry("04:30", 5, "3 ХЕ", 0.25f),
    DetailedFoodEntry("07:30", 11, "14 ХЕ", 0.78f)
)
