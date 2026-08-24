package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
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
import kotlin.math.roundToInt
import kotlin.math.sqrt

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
    val date: LocalDate? = null,
    val value: Float? = null
)

data class DetailedFoodEntry(
    val timeLabel: String,
    val xIndex: Int,
    val breadUnits: String,
    val heightRatio: Float,
    val date: LocalDate? = null,
    val value: Float? = null
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
private const val DETAILED_GRAPH_DAY_WIDTH_DP = 320
private const val DETAILED_GRAPH_MAX_VALUE = 40f
private const val DETAILED_BREAD_UNITS_MAX_VALUE = 150f
private val DETAILED_GLUCOSE_AXIS_VALUES = listOf("40", "30", "20", "10", "0")
private val DETAILED_BREAD_UNITS_AXIS_VALUES = listOf("150", "112,5", "75", "37,5", "0")
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
    val density = LocalDensity.current
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
    var isDatePickerVisible by remember { mutableStateOf(false) }
    val selectedStartDate = remember(selectedRangeStart) { LocalDate.parse(selectedRangeStart) }
    val selectedEndDate = selectedStartDate
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
    val useTransparentEventBars = isInsulinLayerVisible && isFoodLayerVisible && isActivityLayerVisible

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val graphZoomState = rememberSaveable { mutableStateOf(1f) }
    val graphZoom = graphZoomState.value
    var activeZoomAnchor by remember { mutableStateOf<GraphZoomAnchor?>(null) }
    var pendingZoomScroll by remember { mutableStateOf<Int?>(null) }
    var zoomVisualTranslationPx by remember { mutableStateOf(0f) }
    val graphScrollState = rememberScrollState()
    val screenScrollState = rememberScrollState()
    LaunchedEffect(selectedStartDate, selectedEndDate) {
        graphScrollState.scrollTo(0)
        graphZoomState.value = 1f
        activeZoomAnchor = null
        pendingZoomScroll = null
        zoomVisualTranslationPx = 0f
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
                .safeDrawingPadding()
        ) {
            val contentWidth = maxWidth
            val isVeryShort = maxHeight < 420.dp
            val chartCardHeight = if (isVeryShort) 300.dp else 260.dp
            val bottomCardHeight = if (isVeryShort) 94.dp else 98.dp
            val graphContentWidth = detailedGraphWidth(selectedDaysCount) * graphZoom
            val timeLabels = detailedTimeLabels(selectedStartDate, selectedEndDate, graphZoom)

            LaunchedEffect(graphZoom, pendingZoomScroll) {
                pendingZoomScroll?.let { targetScroll ->
                    withFrameNanos { }
                    graphScrollState.scrollTo(targetScroll)
                    if (pendingZoomScroll == targetScroll) {
                        zoomVisualTranslationPx = 0f
                        pendingZoomScroll = null
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                                        DETAILED_GLUCOSE_AXIS_VALUES.forEach { yValue ->
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
                                            .graphicsLayer { translationX = zoomVisualTranslationPx }
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
                                                    detectTapGestures(
                                                        onDoubleTap = {
                                                            graphZoomState.value = 1f
                                                            activeZoomAnchor = null
                                                            pendingZoomScroll = null
                                                            zoomVisualTranslationPx = 0f
                                                        },
                                                        onTap = { offset ->
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
                                                    )
                                                }
                                                .pinchToZoom(
                                                    onZoomStarted = { centroid ->
                                                        val currentGraphWidth = with(density) {
                                                            (detailedGraphWidth(selectedDaysCount) * graphZoomState.value).toPx()
                                                        }
                                                        val nearestPointIndex = displayedPoints.indices.minByOrNull { index ->
                                                            val pointX = detailedGraphFraction(
                                                                displayedPoints[index].date,
                                                                displayedPoints[index].timeLabel,
                                                                selectedStartDate,
                                                                selectedDaysCount
                                                            ) * currentGraphWidth
                                                            kotlin.math.abs(pointX - centroid.x)
                                                        }
                                                        val pointAnchorFraction = nearestPointIndex?.let { index ->
                                                            val pointFraction = detailedGraphFraction(
                                                                displayedPoints[index].date,
                                                                displayedPoints[index].timeLabel,
                                                                selectedStartDate,
                                                                selectedDaysCount
                                                            )
                                                            val pointX = pointFraction * currentGraphWidth
                                                            pointFraction.takeIf {
                                                                kotlin.math.abs(pointX - centroid.x) <= with(density) { 24.dp.toPx() }
                                                            }
                                                        }
                                                        activeZoomAnchor = GraphZoomAnchor(
                                                            timeFraction = pointAnchorFraction
                                                                ?: (centroid.x / currentGraphWidth).coerceIn(0f, 1f),
                                                            viewportX = centroid.x - graphScrollState.value
                                                        )
                                                    },
                                                    onZoomChange = { zoomChange ->
                                                        val anchor = activeZoomAnchor ?: return@pinchToZoom
                                                        val currentZoom = graphZoomState.value
                                                        val nextZoom = (currentZoom * zoomChange).coerceIn(1f, 4f)
                                                        if (nextZoom != currentZoom) {
                                                            val newGraphWidthPx = with(density) {
                                                                (detailedGraphWidth(selectedDaysCount) * nextZoom).toPx()
                                                            }
                                                            val targetScroll = (anchor.timeFraction * newGraphWidthPx - anchor.viewportX)
                                                                .roundToInt()
                                                                .coerceAtLeast(0)
                                                            pendingZoomScroll = targetScroll
                                                            zoomVisualTranslationPx = (graphScrollState.value - targetScroll).toFloat()
                                                            graphZoomState.value = nextZoom
                                                        }
                                                    },
                                                    onZoomEnded = {
                                                        activeZoomAnchor = null
                                                    }
                                                )
                                        ) {
                                            val activityTrackHeight = 9.dp.toPx()
                                            val chartHeight = size.height - activityTrackHeight
                                            val dash = PathEffect.dashPathEffect(floatArrayOf(7f, 7f), 0f)
                                            DETAILED_GLUCOSE_AXIS_VALUES.forEachIndexed { index, _ ->
                                                val y = index.toFloat() / 4f * chartHeight
                                                drawLine(
                                                    color = Color(0xFFE1E4E8),
                                                    start = Offset(0f, y),
                                                    end = Offset(size.width, y),
                                                    pathEffect = dash,
                                                    strokeWidth = 1.dp.toPx()
                                                )
                                            }

                                            if (selectedDaysCount > 1) {
                                                (1 until selectedDaysCount).forEach { dayIndex ->
                                                    val x = size.width * dayIndex / selectedDaysCount
                                                    drawLine(
                                                        color = Color(0xFFD3DAE0),
                                                        start = Offset(x, 0f),
                                                        end = Offset(x, chartHeight),
                                                        strokeWidth = 1.dp.toPx()
                                                    )
                                                }
                                            }

                                            val chartEvents = buildHourlyChartEvents(
                                                foodEntries = if (isFoodLayerVisible) displayedFoodEntries else emptyList(),
                                                insulinEntries = if (isInsulinLayerVisible) displayedInsulinEntries else emptyList(),
                                                rangeStart = selectedStartDate,
                                                daysCount = selectedDaysCount,
                                                canvasWidth = size.width
                                            )

                                            val eventLabelPaint = createEventLabelPaint(11.sp.toPx())
                                            layoutDetailedChartEvents(
                                                events = chartEvents,
                                                chartHeight = chartHeight,
                                                labelPaint = eventLabelPaint
                                            ).forEach { event ->
                                                drawEventBar(
                                                    x = event.x,
                                                    chartHeight = chartHeight,
                                                    barHeight = event.barHeight,
                                                    color = event.color,
                                                    label = event.label,
                                                    labelTop = event.labelTop,
                                                    labelPaint = eventLabelPaint,
                                                    barAlpha = if (useTransparentEventBars) 0.45f else 1f
                                                )
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

                                        if (selectedDaysCount == 1 && maxPointIndex >= 0) {
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

                                        if (selectedDaysCount == 1 && minPointIndex >= 0 && minPointIndex != maxPointIndex) {
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

                                    Box(
                                        modifier = Modifier
                                            .width(graphContentWidth)
                                            .height(30.dp)
                                            .graphicsLayer { translationX = zoomVisualTranslationPx }
                                    ) {
                                        timeLabels.forEach { label ->
                                            Text(
                                                text = label.text,
                                                fontSize = 12.sp,
                                                color = DetailedChartTextSecondary,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.offset(
                                                    x = (graphContentWidth * label.fraction - 16.dp)
                                                        .coerceIn(0.dp, graphContentWidth - 32.dp)
                                                )
                                            )
                                        }
                                    }

                                }

                                Column(
                                    modifier = Modifier
                                        .width(32.dp)
                                        .fillMaxHeight()
                                        .padding(bottom = 18.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.End
                                ) {
                                    DETAILED_BREAD_UNITS_AXIS_VALUES.forEach { yValue ->
                                        Text(
                                            text = yValue,
                                            fontSize = 10.sp,
                                            color = Color(0xFFFF8058),
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))


                        Column(
                            modifier = Modifier
                                .width(42.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LayerToggleButton(
                                iconRes = R.drawable.ic_syringe_blue,
                                isActive = isInsulinLayerVisible,
                                activeBgColor = Color(0xFFE0F6FF),
                                activeTint = Color(0xFF38B7E1),
                                onClick = { isInsulinLayerVisible = !isInsulinLayerVisible }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LayerToggleButton(
                                iconRes = R.drawable.ic_spoon_and_fork_orange,
                                isActive = isFoodLayerVisible,
                                activeBgColor = Color(0xFFFFE7DF),
                                activeTint = Color(0xFFFF8058),
                                onClick = { isFoodLayerVisible = !isFoodLayerVisible }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LayerToggleButton(
                                iconRes = R.drawable.ic_walking_blue,
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
                initialDate = selectedStartDate,
                dayStatuses = dayStatuses,
                onDismissRequest = { isDatePickerVisible = false },
                onDateRangeSelected = { start, _ ->
                    selectedRangeStart = start.toString()
                    selectedPointIndex = null
                    onDateRangeSelected(start, start)
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
    val minutesFromStart = detailedGraphMinute(date, timeLabel, rangeStart, daysCount)
    return (minutesFromStart.toFloat() / (daysCount * 24 * 60)).coerceIn(0f, 1f)
}

private fun detailedGraphMinute(
    date: LocalDate?,
    timeLabel: String,
    rangeStart: LocalDate,
    daysCount: Int
): Long {
    val dayOffset = org.threeten.bp.temporal.ChronoUnit.DAYS
        .between(rangeStart, date ?: rangeStart)
        .coerceIn(0, (daysCount - 1).toLong())
    return dayOffset * 24 * 60 + timeLabelToMinutes(timeLabel)
}

private data class DetailedChartEvent(
    val timestampMinutes: Long,
    val x: Float,
    val heightRatio: Float,
    val color: Color,
    val label: String,
    val order: Int
)

private data class GraphZoomAnchor(
    val timeFraction: Float,
    val viewportX: Float
)

private data class HourlyChartValue(
    val hourStartMinutes: Long,
    val value: Float
)

private fun buildHourlyChartEvents(
    foodEntries: List<DetailedFoodEntry>,
    insulinEntries: List<DetailedInsulinEntry>,
    rangeStart: LocalDate,
    daysCount: Int,
    canvasWidth: Float
): List<DetailedChartEvent> {
    val totalMinutes = daysCount * 24 * 60
    fun hourCenterX(hourStartMinutes: Long): Float =
        ((hourStartMinutes + 30).toFloat() / totalMinutes)
            .coerceIn(0f, 1f) * canvasWidth

    val hourlyFood = foodEntries
        .groupBy { food ->
            detailedGraphMinute(food.date, food.timeLabel, rangeStart, daysCount) / 60L
        }
        .map { (hourIndex, entries) ->
            HourlyChartValue(
                hourStartMinutes = hourIndex * 60L,
                value = entries.fold(0f) { total, entry -> total + entry.resolvedValue() }
            )
        }
        .sortedBy { it.hourStartMinutes }

    val hourlyInsulin = insulinEntries
        .groupBy { insulin ->
            detailedGraphMinute(insulin.date, insulin.timeLabel, rangeStart, daysCount) / 60L
        }
        .map { (hourIndex, entries) ->
            HourlyChartValue(
                hourStartMinutes = hourIndex * 60L,
                value = entries.fold(0f) { total, entry -> total + entry.resolvedValue() }
            )
        }
        .sortedBy { it.hourStartMinutes }

    val foodEvents = hourlyFood.mapIndexed { index, event ->
        DetailedChartEvent(
            timestampMinutes = event.hourStartMinutes + 30L,
            x = hourCenterX(event.hourStartMinutes),
            heightRatio = (event.value / DETAILED_BREAD_UNITS_MAX_VALUE).coerceIn(0f, 1f),
            color = Color(0xFFFF8058),
            label = detailedEventLabel(event.value, "ХЕ"),
            order = index
        )
    }

    val insulinEvents = hourlyInsulin.mapIndexed { index, event ->
        DetailedChartEvent(
            timestampMinutes = event.hourStartMinutes + 30L,
            x = hourCenterX(event.hourStartMinutes),
            heightRatio = (event.value / DETAILED_BREAD_UNITS_MAX_VALUE).coerceIn(0f, 1f),
            color = Color(0xFF38B7E1),
            label = detailedEventLabel(event.value, "Ед."),
            order = hourlyFood.size + index
        )
    }

    return foodEvents + insulinEvents
}

private fun DetailedFoodEntry.resolvedValue(): Float =
    value ?: breadUnits.toDetailedEventValue()

private fun DetailedInsulinEntry.resolvedValue(): Float =
    value ?: units.toDetailedEventValue()

private fun String.toDetailedEventValue(): Float =
    trim()
        .substringBefore(' ')
        .replace(',', '.')
        .toFloatOrNull()
        ?: 0f

private fun detailedEventLabel(value: Float, unit: String): String =
    "${String.format(java.util.Locale.US, "%.1f", value)} $unit"

private data class PositionedDetailedChartEvent(
    val x: Float,
    val barHeight: Float,
    val color: Color,
    val label: String,
    val labelTop: Float
)

private data class EventLabelCandidate(
    val event: DetailedChartEvent,
    val x: Float,
    val labelWidth: Float,
    val labelLeft: Float
) {
    val labelRight: Float get() = labelLeft + labelWidth
}

private fun createEventLabelPaint(textSize: Float): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.White.toArgb()
    this.textSize = textSize
    textAlign = Paint.Align.CENTER
    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
}

private fun DrawScope.layoutDetailedChartEvents(
    events: List<DetailedChartEvent>,
    chartHeight: Float,
    labelPaint: Paint
): List<PositionedDetailedChartEvent> {
    if (events.isEmpty()) return emptyList()

    val barWidth = 18.dp.toPx()
    val barGap = 4.dp.toPx()
    val labelHorizontalPadding = 7.dp.toPx()
    val labelHeight = 20.dp.toPx()
    val labelGap = 4.dp.toPx()
    val maxBarX = (size.width - barWidth / 2f).coerceAtLeast(barWidth / 2f)

    val separatedEvents = events
        .groupBy { it.timestampMinutes }
        .values
        .flatMap { sameMinuteEvents ->
            val orderedEvents = sameMinuteEvents.sortedBy { it.order }
            val centerOffset = (orderedEvents.size - 1) / 2f
            val rawXs = orderedEvents.mapIndexed { index, event ->
                event.x + (index - centerOffset) * (barWidth + barGap)
            }
            val shift = when {
                rawXs.first() < barWidth / 2f -> barWidth / 2f - rawXs.first()
                rawXs.last() > maxBarX -> maxBarX - rawXs.last()
                else -> 0f
            }

            orderedEvents.mapIndexed { index, event ->
                event to (rawXs[index] + shift).coerceIn(barWidth / 2f, maxBarX)
            }
        }
        .sortedBy { (_, x) -> x }

    val candidates = separatedEvents.map { (event, x) ->
        val labelWidth = labelPaint.measureText(event.label) + labelHorizontalPadding * 2
        val labelLeft = eventLabelLeft(x, labelWidth, size.width)
        EventLabelCandidate(event, x, labelWidth, labelLeft)
    }

    val labelClusters = mutableListOf<MutableList<EventLabelCandidate>>()
    candidates.forEach { candidate ->
        val currentCluster = labelClusters.lastOrNull()
        if (currentCluster == null || candidate.labelLeft > currentCluster.maxOf { it.labelRight } + labelGap) {
            labelClusters += mutableListOf(candidate)
        } else {
            currentCluster += candidate
        }
    }

    return labelClusters.flatMap { cluster ->
        val highestBarTop = cluster.minOf { candidate ->
            chartHeight - chartHeight * candidate.event.heightRatio
        }
        val stackHeight = cluster.size * labelHeight + (cluster.size - 1) * labelGap
        val stackTop = (highestBarTop - labelGap - stackHeight).coerceAtLeast(0f)

        cluster.mapIndexed { laneIndex, candidate ->
            val barHeight = chartHeight * candidate.event.heightRatio
            PositionedDetailedChartEvent(
                x = candidate.x,
                barHeight = barHeight,
                color = candidate.event.color,
                label = candidate.event.label,
                labelTop = stackTop + laneIndex * (labelHeight + labelGap)
            )
        }
    }
}

private fun eventLabelLeft(x: Float, labelWidth: Float, canvasWidth: Float): Float =
    (x - labelWidth / 2f).coerceIn(0f, (canvasWidth - labelWidth).coerceAtLeast(0f))

private fun DrawScope.drawEventBar(
    x: Float,
    chartHeight: Float,
    barHeight: Float,
    color: Color,
    label: String,
    labelTop: Float,
    labelPaint: Paint,
    barAlpha: Float
) {
    val barWidth = 18.dp.toPx()
    val barTop = chartHeight - barHeight
    drawRoundRect(
        color = color.copy(alpha = barAlpha),
        topLeft = Offset(x - barWidth / 2f, barTop),
        size = Size(barWidth, barHeight),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )

    val horizontalPadding = 7.dp.toPx()
    val labelHeight = 20.dp.toPx()
    val labelWidth = labelPaint.measureText(label) + horizontalPadding * 2
    val labelLeft = eventLabelLeft(x, labelWidth, size.width)

    drawRoundRect(
        color = color,
        topLeft = Offset(labelLeft, labelTop),
        size = Size(labelWidth, labelHeight),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    val textBaseline = labelTop + (labelHeight - labelPaint.descent() - labelPaint.ascent()) / 2f
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawText(label, labelLeft + labelWidth / 2f, textBaseline, labelPaint)
    }
}

private fun Modifier.pinchToZoom(
    onZoomStarted: (Offset) -> Unit,
    onZoomChange: (Float) -> Unit,
    onZoomEnded: () -> Unit
): Modifier =
    pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var previousDistance: Float? = null
            var isZoomGesture = false

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val pointers = event.changes.filter { it.pressed }
                if (pointers.size >= 2) {
                    val first = pointers[0].position
                    val second = pointers[1].position
                    val distance = sqrt(
                        (second.x - first.x) * (second.x - first.x) +
                            (second.y - first.y) * (second.y - first.y)
                    )
                    if (previousDistance == null) {
                        onZoomStarted(
                            Offset(
                                x = (first.x + second.x) / 2f,
                                y = (first.y + second.y) / 2f
                            )
                        )
                        isZoomGesture = true
                    }
                    previousDistance?.takeIf { it > 0f }?.let { previous ->
                        val zoomChange = distance / previous
                        if (kotlin.math.abs(zoomChange - 1f) > 0.01f) {
                            onZoomChange(zoomChange)
                            pointers.forEach { it.consume() }
                        }
                    }
                    previousDistance = distance
                } else {
                    previousDistance = null
                }

                if (event.changes.none { it.pressed }) break
            }

            if (isZoomGesture) onZoomEnded()
        }
    }

private data class DetailedTimelineLabel(
    val fraction: Float,
    val text: String
)

private fun detailedTimeLabels(
    start: LocalDate,
    end: LocalDate,
    zoom: Float
): List<DetailedTimelineLabel> {
    val daysCount = daysInDetailedRange(start, end)
    if (daysCount == 1) {
        val stepMinutes = when {
            zoom >= 3f -> 15
            zoom >= 2f -> 30
            else -> 60
        }
        val totalMinutes = 24 * 60
        return (0..totalMinutes step stepMinutes).map { minuteOffset ->
            DetailedTimelineLabel(
                fraction = minuteOffset.toFloat() / totalMinutes,
                text = String.format(
                    java.util.Locale.US,
                    "%02d:%02d",
                    minuteOffset / 60,
                    minuteOffset % 60
                )
            )
        }
    }

    val stepHours = when {
        daysCount <= 3 -> 6
        else -> 24
    }
    val totalHours = daysCount * 24
    val hourOffsets = 0 until totalHours step stepHours
    return hourOffsets.map { hourOffset ->
        val date = start.plusDays((hourOffset / 24).toLong().coerceAtMost((daysCount - 1).toLong()))
        val text = when {
            stepHours == 24 -> "${date.dayOfMonth} ${DETAILED_MONTHS_SHORT[date.monthValue - 1]}"
            else -> "${date.dayOfMonth} ${DETAILED_MONTHS_SHORT[date.monthValue - 1]}\n${String.format(java.util.Locale.US, "%02d:00", hourOffset % 24)}"
        }
        DetailedTimelineLabel(
            fraction = hourOffset.toFloat() / totalHours,
            text = text
        )
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
    DetailedInsulinEntry("04:30", 5, "15,5 Ед.", 15.5f / 150f, value = 15.5f),
    DetailedInsulinEntry("06:00", 8, "4,1 Ед.", 4.1f / 150f, value = 4.1f),
    DetailedInsulinEntry("09:00", 14, "7,3 Ед.", 7.3f / 150f, value = 7.3f)
)

fun defaultDetailedFoodEntries(): List<DetailedFoodEntry> = listOf(
    DetailedFoodEntry("03:00", 2, "6,5 ХЕ", 6.5f / 150f, value = 6.5f),
    DetailedFoodEntry("04:30", 5, "3 ХЕ", 3f / 150f, value = 3f),
    DetailedFoodEntry("07:30", 11, "14 ХЕ", 14f / 150f, value = 14f)
)
