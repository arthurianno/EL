package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.home.interactor.buildDailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.DailyGlucoseModel
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.records.mapper.DetailedChartItemsBuilder
import org.threeten.bp.LocalDate

data class DetailedGlucosePoint(
    val timeLabel: String,
    val value: Float,
    val date: LocalDate? = null,
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
    val heightRatio: Float,
    val date: LocalDate? = null
)

data class DetailedFoodEntry(
    val timeLabel: String,
    val xIndex: Int,
    val breadUnits: String,
    val heightRatio: Float,
    val date: LocalDate? = null
)

data class DetailedActivityEntry(
    val startTimeLabel: String,
    val endTimeLabel: String,
    val durationMins: Long,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)

private val DetailedChartBackground = Color(0xFF1FBFD2)
private val DetailedChartCardBorder = Color(0xFFA4A4A4)
private val DetailedChartTextPrimary = Color(0xFF3D4556)
private val DetailedChartTextSecondary = Color(0xFF878B93)
private val DetailedLowColor = Color(0xFFD93B17)
private val DetailedNormalColor = Color(0xFF29AF99)
private val DetailedHighColor = Color(0xFFEE9C17)
private const val DETAILED_GRAPH_HOUR_WIDTH_DP = 72
private const val DETAILED_GRAPH_DAY_WIDTH_DP = 240
private const val DETAILED_GRAPH_MAX_VALUE = 16f
private const val DETAILED_BADGE_WIDTH_DP = 70
private const val DETAILED_BADGE_HEIGHT_DP = 22
private const val DETAILED_BADGE_POINT_GAP_DP = 4

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
    activityEntries: List<DetailedActivityEntry> = emptyList(),
    dailyGlucoseModel: DailyGlucoseModel? = null,
    allEvents: List<EventV2> = emptyList(),
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit = { _, _ -> }
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

    val initialLocalDate = remember(initialDate) { initialDate.toDetailedLocalDate() }
    var selectedRangeStart by rememberSaveable { mutableStateOf(initialLocalDate.toString()) }
    var selectedRangeEnd by rememberSaveable { mutableStateOf(initialLocalDate.toString()) }
    var isDatePickerVisible by remember { mutableStateOf(false) }
    val selectedStartDate = remember(selectedRangeStart) { LocalDate.parse(selectedRangeStart) }
    val selectedEndDate = remember(selectedRangeEnd) { LocalDate.parse(selectedRangeEnd) }
    val selectedRangeTitle = remember(selectedStartDate, selectedEndDate) {
        formatDetailedDateRange(selectedStartDate, selectedEndDate)
    }
    val selectedDaysCount = remember(selectedStartDate, selectedEndDate) {
        daysInDetailedRange(selectedStartDate, selectedEndDate)
    }

    // Layer toggles
    var isInsulinLayerVisible by remember { mutableStateOf(true) }
    var isFoodLayerVisible by remember { mutableStateOf(true) }
    var isActivityLayerVisible by remember { mutableStateOf(true) }

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val graphScrollState = rememberScrollState()
    val screenScrollState = rememberScrollState()
    LaunchedEffect(selectedStartDate, selectedEndDate) {
        graphScrollState.scrollTo(0)
        selectedPointIndex = null
    }

    val selectedRangeEvents = remember(allEvents, selectedStartDate, selectedEndDate) {
        allEvents.filter { event ->
            val date = event.additionTime.toLocalDate()
            !date.isBefore(selectedStartDate) && !date.isAfter(selectedEndDate)
        }
    }
    val selectedRangeModel = remember(dailyGlucoseModel, selectedRangeEvents) {
        dailyGlucoseModel?.let { model ->
            buildDailyGlucoseModel(
                selectedRangeEvents,
                model.glucoseLevelSettings,
                model.glucoseFormat
            )
        }
    }
    val isInitialSingleDay = selectedStartDate == initialLocalDate && selectedEndDate == initialLocalDate
    val displayedPoints = remember(selectedRangeModel, glucosePoints, isInitialSingleDay, selectedRangeEvents) {
        selectedRangeModel?.let { model ->
            DetailedChartItemsBuilder.buildPoints(model, selectedRangeEvents)
        }.orEmpty().ifEmpty {
            if (isInitialSingleDay) glucosePoints else emptyList()
        }
    }
    val displayedInsulinEntries = remember(displayedPoints, selectedRangeEvents, insulinEntries, isInitialSingleDay) {
        if (selectedRangeEvents.isNotEmpty()) {
            DetailedChartItemsBuilder.buildInsulinEntries(displayedPoints, selectedRangeEvents)
        } else if (isInitialSingleDay) {
            insulinEntries
        } else {
            emptyList()
        }
    }
    val displayedFoodEntries = remember(displayedPoints, selectedRangeEvents, foodEntries, isInitialSingleDay) {
        if (selectedRangeEvents.isNotEmpty()) {
            DetailedChartItemsBuilder.buildFoodEntries(displayedPoints, selectedRangeEvents)
        } else if (isInitialSingleDay) {
            foodEntries
        } else {
            emptyList()
        }
    }
    val displayedActivityEntries = remember(selectedRangeEvents, activityEntries, isInitialSingleDay) {
        if (selectedRangeEvents.isNotEmpty()) {
            DetailedChartItemsBuilder.buildActivityEntries(selectedRangeEvents)
        } else if (isInitialSingleDay) {
            activityEntries
        } else {
            emptyList()
        }
    }
    val dayStatuses = remember(allEvents, dailyGlucoseModel) {
        allEvents
            .filter { it.type is EventType.Glucose && it.value != null }
            .groupBy { it.additionTime.toLocalDate() }
            .mapValues { (_, events) ->
                val settings = dailyGlucoseModel?.glucoseLevelSettings
                val values = events.mapNotNull { it.value }
                val hasLow = settings?.let { target -> values.any { it in target.low } }
                    ?: values.any { it <= 3.9 }
                val hasHigh = settings?.let { target -> values.any { it in target.high } }
                    ?: values.any { it >= 10.0 }
                when {
                    hasHigh -> DayGlycemicStatus.HIGH
                    hasLow -> DayGlycemicStatus.LOW
                    else -> DayGlycemicStatus.NORM
                }
            }
    }

    val totalPoints = displayedPoints.size
    val hasData = totalPoints >= 2
    val averageValue = if (hasData) displayedPoints.map { it.value }.average().toFloat() else 0f
    val averageValueText = if (hasData) {
        String.format(java.util.Locale.US, "%.1f", averageValue).replace('.', ',')
    } else {
        "-"
    }
    val normalCount = displayedPoints.count { it.value in 3.91f..9.99f }
    val highCount = displayedPoints.count { it.value >= 10.0f }
    val lowCount = displayedPoints.count { it.value <= 3.9f }
    val normalPercent = if (totalPoints > 0) normalCount * 100 / totalPoints else 0
    val highPercent = if (totalPoints > 0) highCount * 100 / totalPoints else 0
    val lowPercent = if (totalPoints > 0) lowCount * 100 / totalPoints else 0
    val sdValue = if (hasData) {
        val variance = displayedPoints.map { point ->
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
            val contentWidth = maxWidth
            val isVeryShort = maxHeight < 420.dp
            val chartCardHeight = if (isVeryShort) 164.dp else 234.dp
            val bottomCardHeight = if (isVeryShort) 94.dp else 98.dp
            val graphContentWidth = detailedGraphWidth(selectedDaysCount)
            val timeLabels = detailedTimeLabels(selectedStartDate, selectedEndDate)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .verticalScroll(screenScrollState)
                    .padding(
                        top = if (isVeryShort) 12.dp else 18.dp,
                        bottom = if (isVeryShort) 12.dp else 18.dp
                    ),
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
                                        text = selectedRangeTitle,
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
                                        .padding(bottom = 18.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
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
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .horizontalScroll(graphScrollState)
                                ) {
                                    BoxWithConstraints(
                                        modifier = Modifier
                                            .width(graphContentWidth)
                                            .weight(1f)
                                    ) {
                                        val graphWidth = graphContentWidth
                                        val maxPoint = displayedPoints.maxByOrNull { it.value }
                                        val minPoint = displayedPoints.minByOrNull { it.value }
                                        val maxPointIndex = maxPoint?.let { displayedPoints.indexOf(it) } ?: -1
                                        val minPointIndex = minPoint?.let { displayedPoints.indexOf(it) } ?: -1

                                        Canvas(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .pointerInput(displayedPoints) {
                                                    detectTapGestures { offset ->
                                                        if (displayedPoints.isNotEmpty()) {
                                                            selectedPointIndex = displayedPoints.indices.minByOrNull { index ->
                                                                val pointX = detailedGraphFraction(
                                                                    displayedPoints[index].date,
                                                                    displayedPoints[index].timeLabel,
                                                                    selectedStartDate,
                                                                    selectedDaysCount
                                                                ) * size.width
                                                                kotlin.math.abs(pointX - offset.x)
                                                            }
                                                        }
                                                    }
                                                }
                                        ) {
                                            val activityTrackHeight = 9.dp.toPx()
                                            val chartHeight = size.height - activityTrackHeight
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
                                                displayedFoodEntries.forEach { food ->
                                                    val x = detailedGraphFraction(
                                                        food.date,
                                                        food.timeLabel,
                                                        selectedStartDate,
                                                        selectedDaysCount
                                                    ) * size.width
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
                                                displayedInsulinEntries.forEach { insulin ->
                                                    val x = detailedGraphFraction(
                                                        insulin.date,
                                                        insulin.timeLabel,
                                                        selectedStartDate,
                                                        selectedDaysCount
                                                    ) * size.width
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

                                            val linePoints = displayedPoints.map { point ->
                                                val pointRadius = 8.dp.toPx()
                                                Offset(
                                                    x = detailedGraphFraction(
                                                        point.date,
                                                        point.timeLabel,
                                                        selectedStartDate,
                                                        selectedDaysCount
                                                    ) * size.width,
                                                    y = (chartHeight - (point.value / DETAILED_GRAPH_MAX_VALUE).coerceIn(0f, 1f) * chartHeight)
                                                        .coerceIn(pointRadius, chartHeight - pointRadius)
                                                )
                                            }

                                            if (linePoints.size > 1) {
                                                for (index in 0 until linePoints.lastIndex) {
                                                    val start = linePoints[index]
                                                    val end = linePoints[index + 1]
                                                    val value = displayedPoints[index].value
                                                    if (displayedPoints[index].date != null &&
                                                        displayedPoints[index].date != displayedPoints[index + 1].date
                                                    ) {
                                                        continue
                                                    }
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
                                                    color = detailedPointColor(displayedPoints[index].value),
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
                                                displayedActivityEntries.forEach { activityEntry ->
                                                    val start = detailedGraphFraction(
                                                        activityEntry.startDate,
                                                        activityEntry.startTimeLabel,
                                                        selectedStartDate,
                                                        selectedDaysCount
                                                    ) * size.width
                                                    val end = detailedGraphFraction(
                                                        activityEntry.endDate ?: activityEntry.startDate,
                                                        activityEntry.endTimeLabel,
                                                        selectedStartDate,
                                                        selectedDaysCount
                                                    ) * size.width
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

                                        if (displayedPoints.isEmpty()) {
                                            Text(
                                                text = "Нет измерений за выбранный период",
                                                fontSize = 13.sp,
                                                color = DetailedChartTextSecondary,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }

                                        if (maxPointIndex >= 0) {
                                            val point = displayedPoints[maxPointIndex].toGraphOffset(
                                                graphWidth,
                                                maxHeight,
                                                selectedStartDate,
                                                selectedDaysCount
                                            )
                                            PeakBadgeOverlay(
                                                text = "max ${String.format(java.util.Locale.US, "%.1f", displayedPoints[maxPointIndex].value).replace('.', ',')}",
                                                bgColor = DetailedHighColor,
                                                modifier = Modifier.padding(
                                                    start = detailedBadgeStart(point.x, graphWidth, preferRight = true),
                                                    top = detailedBadgeTop(point.y, maxHeight, preferAbove = true)
                                                )
                                            )
                                        }

                                        if (minPointIndex >= 0 && minPointIndex != maxPointIndex) {
                                            val point = displayedPoints[minPointIndex].toGraphOffset(
                                                graphWidth,
                                                maxHeight,
                                                selectedStartDate,
                                                selectedDaysCount
                                            )
                                            PeakBadgeOverlay(
                                                text = "min ${String.format(java.util.Locale.US, "%.1f", displayedPoints[minPointIndex].value).replace('.', ',')}",
                                                bgColor = DetailedLowColor,
                                                modifier = Modifier.padding(
                                                    start = detailedBadgeStart(point.x, graphWidth, preferRight = false),
                                                    top = detailedBadgeTop(point.y, maxHeight, preferAbove = false)
                                                )
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .width(graphContentWidth)
                                            .height(18.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        timeLabels.forEach { label ->
                                            Text(
                                                text = label,
                                                fontSize = 12.sp,
                                                color = DetailedChartTextSecondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(
                            modifier = Modifier
                                .width(42.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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
                        .padding(horizontal = 30.dp, vertical = if (isVeryShort) 8.dp else 10.dp)
                ) {
                    val selectedPoint = selectedPointIndex?.let { displayedPoints.getOrNull(it) }

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
                                    text = if (selectedDaysCount == 1) "События дня" else "События периода",
                                    fontSize = 12.sp,
                                    color = DetailedChartTextSecondary
                                )
                                Text(
                                    text = selectedRangeTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DetailedChartTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }

                            DetailedVerticalDivider()

                            Column(
                                modifier = Modifier.width(116.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = averageValueText,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DetailedHighColor,
                                    letterSpacing = 0.sp,
                                    lineHeight = 34.sp
                                )
                                Text(
                                    text = "ммоль/л",
                                    fontSize = 11.sp,
                                    color = DetailedHighColor,
                                    lineHeight = 11.sp
                                )
                                Text(
                                    text = if (selectedDaysCount == 1) "средний за день" else "средний за период",
                                    fontSize = 10.sp,
                                    color = DetailedChartTextSecondary,
                                    lineHeight = 10.sp
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
                initialStartDate = selectedStartDate,
                initialEndDate = selectedEndDate,
                dayStatuses = dayStatuses,
                onDismissRequest = { isDatePickerVisible = false },
                onDateRangeSelected = { start, end ->
                    selectedRangeStart = start.toString()
                    selectedRangeEnd = end.toString()
                    selectedPointIndex = null
                    onDateRangeSelected(start, end)
                    isDatePickerVisible = false
                }
            )
        }
    }
}

private fun daysInDetailedRange(start: LocalDate, end: LocalDate): Int =
    org.threeten.bp.temporal.ChronoUnit.DAYS.between(start, end).toInt() + 1

private fun detailedGraphWidth(daysCount: Int): Dp =
    if (daysCount == 1) {
        (24 * DETAILED_GRAPH_HOUR_WIDTH_DP).dp
    } else {
        (daysCount * DETAILED_GRAPH_DAY_WIDTH_DP).dp
    }

private fun detailedGraphFraction(
    date: LocalDate?,
    timeLabel: String,
    rangeStart: LocalDate,
    daysCount: Int
): Float {
    val dayOffset = org.threeten.bp.temporal.ChronoUnit.DAYS
        .between(rangeStart, date ?: rangeStart)
        .coerceIn(0, (daysCount - 1).toLong())
    val minutesFromStart = dayOffset * 24 * 60 + timeLabelToMinutes(timeLabel)
    return (minutesFromStart.toFloat() / (daysCount * 24 * 60)).coerceIn(0f, 1f)
}

private fun detailedTimeLabels(start: LocalDate, end: LocalDate): List<String> {
    val daysCount = daysInDetailedRange(start, end)
    val stepHours = when {
        daysCount == 1 -> 1
        daysCount <= 3 -> 6
        else -> 24
    }
    val totalHours = daysCount * 24
    return (0..totalHours step stepHours).map { hourOffset ->
        val date = start.plusDays((hourOffset / 24).toLong().coerceAtMost((daysCount - 1).toLong()))
        when {
            daysCount == 1 -> String.format(java.util.Locale.US, "%02d:00", hourOffset)
            stepHours == 24 -> "${date.dayOfMonth} ${DETAILED_MONTHS_SHORT[date.monthValue - 1]}"
            else -> "${date.dayOfMonth} ${DETAILED_MONTHS_SHORT[date.monthValue - 1]}\n${String.format(java.util.Locale.US, "%02d:00", hourOffset % 24)}"
        }
    }
}

private fun formatDetailedDateRange(start: LocalDate, end: LocalDate): String =
    if (start == end) {
        formatDetailedDate(start)
    } else {
        "${formatDetailedDate(start)} — ${formatDetailedDate(end)}"
    }

private fun formatDetailedDate(date: LocalDate): String =
    "${date.dayOfMonth} ${DETAILED_MONTHS_GENITIVE[date.monthValue - 1]} ${date.year}"

private val DETAILED_MONTHS_GENITIVE = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря"
)

private val DETAILED_MONTHS_SHORT = listOf(
    "янв.", "фев.", "мар.", "апр.", "мая", "июн.",
    "июл.", "авг.", "сен.", "окт.", "ноя.", "дек."
)

private fun String.toDetailedLocalDate(): LocalDate {
    val months = listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    )
    return runCatching {
        val parts = trim().split(" ")
        val day = parts.getOrNull(0)?.toIntOrNull() ?: return@runCatching LocalDate.now()
        val month = months.indexOf(parts.getOrNull(1)?.lowercase()).takeIf { it >= 0 }?.plus(1)
            ?: return@runCatching LocalDate.now()
        val year = parts.getOrNull(2)?.toIntOrNull() ?: return@runCatching LocalDate.now()
        LocalDate.of(year, month, day)
    }.getOrDefault(LocalDate.now())
}

private fun DetailedGlucosePoint.toGraphOffset(
    graphWidth: Dp,
    graphHeight: Dp,
    rangeStart: LocalDate,
    daysCount: Int
): DpOffset {
    val x = graphWidth * detailedGraphFraction(date, timeLabel, rangeStart, daysCount)
    val yRatio = (value / DETAILED_GRAPH_MAX_VALUE).coerceIn(0f, 1f)
    val chartHeight = (graphHeight - 9.dp).coerceAtLeast(0.dp)
    val pointRadius = 8.dp
    return DpOffset(
        x = x,
        y = (chartHeight * (1f - yRatio)).coerceIn(pointRadius, chartHeight - pointRadius)
    )
}

private fun detailedBadgeStart(pointX: Dp, graphWidth: Dp, preferRight: Boolean): Dp {
    val badgeWidth = DETAILED_BADGE_WIDTH_DP.dp
    val gap = DETAILED_BADGE_POINT_GAP_DP.dp
    val preferred = if (preferRight) pointX + gap else pointX - badgeWidth - gap
    val fallback = if (preferRight) pointX - badgeWidth - gap else pointX + gap
    val maxStart = (graphWidth - badgeWidth).coerceAtLeast(0.dp)
    return when {
        preferred in 0.dp..maxStart -> preferred
        fallback in 0.dp..maxStart -> fallback
        else -> preferred.coerceIn(0.dp, maxStart)
    }
}

private fun detailedBadgeTop(pointY: Dp, graphHeight: Dp, preferAbove: Boolean): Dp {
    val badgeHeight = DETAILED_BADGE_HEIGHT_DP.dp
    val gap = DETAILED_BADGE_POINT_GAP_DP.dp
    val preferred = if (preferAbove) pointY - badgeHeight - gap else pointY + gap
    val fallback = if (preferAbove) pointY + gap else pointY - badgeHeight - gap
    val maxTop = (graphHeight - badgeHeight).coerceAtLeast(0.dp)
    return when {
        preferred in 0.dp..maxTop -> preferred
        fallback in 0.dp..maxTop -> fallback
        else -> preferred.coerceIn(0.dp, maxTop)
    }
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
