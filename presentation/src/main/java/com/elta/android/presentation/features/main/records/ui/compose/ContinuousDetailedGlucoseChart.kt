package com.elta.android.presentation.features.main.records.ui.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.max
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.home.interactor.buildDailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.DailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.records.mapper.DetailedChartItemsBuilder
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.ChronoUnit
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.sqrt

private const val DAY_MINUTES = 24L * 60L
private const val MIN_VIEWPORT_MINUTES = 60L
private const val INITIAL_VIEWPORT_MINUTES = DAY_MINUTES
private const val MAX_VIEWPORT_MINUTES = 30L * DAY_MINUTES
private const val RAW_DETAILS_MAX_MINUTES = 2L * DAY_MINUTES
private const val INTERMEDIATE_DETAILS_MAX_MINUTES = 6L * DAY_MINUTES
private const val INTERMEDIATE_BUCKET_MINUTES = 3L * 60L
private const val MAX_GLUCOSE = 16f

private val ContinuousBackground get() = NewDesignPaletteController.colors.normalEnd
private val ContinuousPrimary = Color(0xFF3D4556)
private val ContinuousSecondary = Color(0xFF878B93)
private val ContinuousBorder = Color(0xFFA4A4A4)
private val ContinuousLow = Color(0xFFD93B17)
private val ContinuousNormal = GlucoseDashboardTheme.NormalChartColor
private val ContinuousHigh = Color(0xFFEE9C17)

/**
 * The detailed chart is a single real-time viewport. Its position and duration are
 * state, rather than a selected day/week/month mode.
 */
@Composable
internal fun ContinuousDetailedGlucoseChartScreen(
    onBackClick: () -> Unit,
    initialDate: String,
    fallbackGlucosePoints: List<DetailedGlucosePoint>,
    fallbackInsulinEntries: List<DetailedInsulinEntry>,
    fallbackFoodEntries: List<DetailedFoodEntry>,
    fallbackActivityEntries: List<DetailedActivityEntry>,
    dailyGlucoseModel: DailyGlucoseModel?,
    allEvents: List<EventV2>,
    onMonthsNeeded: (LocalDate, LocalDate) -> Unit
) {
    val activity = LocalContext.current.continuousFindActivity()
    DisposableEffect(activity) {
        val previousOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose { if (previousOrientation != null) activity.requestedOrientation = previousOrientation }
    }

    val today = remember(initialDate) { initialDate.toContinuousLocalDate() }
    val historyStartDate = remember(today) { YearMonth.from(today).minusMonths(11).atDay(1) }
    val historyEndDate = remember(today) { today.plusDays(1) }
    val historyDuration = remember(historyStartDate, historyEndDate) {
        ChronoUnit.DAYS.between(historyStartDate, historyEndDate) * DAY_MINUTES
    }
    var viewportStart by remember(historyStartDate, historyEndDate) {
        mutableStateOf((historyDuration - INITIAL_VIEWPORT_MINUTES).coerceAtLeast(0L))
    }
    var viewportDuration by remember(historyStartDate, historyEndDate) {
        mutableStateOf(INITIAL_VIEWPORT_MINUTES.coerceAtMost(historyDuration))
    }
    var isDatePickerVisible by remember { mutableStateOf(false) }
    var selectedPoint by remember { mutableStateOf<DetailedGlucosePoint?>(null) }
    var insulinVisible by remember { mutableStateOf(true) }
    var foodVisible by remember { mutableStateOf(true) }
    var activityVisible by remember { mutableStateOf(true) }
    val maxViewportDuration = MAX_VIEWPORT_MINUTES.coerceAtMost(historyDuration)

    fun updateViewport(start: Long, duration: Long) {
        val safeDuration = duration.coerceIn(MIN_VIEWPORT_MINUTES.coerceAtMost(historyDuration), maxViewportDuration)
        viewportDuration = safeDuration
        viewportStart = start.coerceIn(0L, (historyDuration - safeDuration).coerceAtLeast(0L))
        selectedPoint = null
    }

    val viewportEnd = viewportStart + viewportDuration
    val viewportStartDate = historyStartDate.plusDays(viewportStart / DAY_MINUTES)
    val viewportEndDate = historyStartDate.plusDays(((viewportEnd - 1).coerceAtLeast(0L)) / DAY_MINUTES)
    val viewportTitle = remember(viewportStartDate, viewportEndDate) {
        continuousFormatDateRange(viewportStartDate, viewportEndDate)
    }

    // Request only months touched by the viewport. The parent keeps a month cache,
    // including successful empty responses, so panning back never reloads a month.
    LaunchedEffect(viewportStartDate, viewportEndDate) {
        onMonthsNeeded(viewportStartDate, viewportEndDate)
    }

    val timelineModel = remember(allEvents, dailyGlucoseModel) {
        dailyGlucoseModel?.let {
            buildDailyGlucoseModel(allEvents, it.glucoseLevelSettings, it.glucoseFormat)
        }
    }
    val realPoints = remember(timelineModel, allEvents, fallbackGlucosePoints) {
        timelineModel?.let { DetailedChartItemsBuilder.buildPoints(it, allEvents) }
            .orEmpty()
            .ifEmpty { fallbackGlucosePoints }
            .sortedBy { it.continuousMinute(historyStartDate) }
    }
    val insulinEntries = remember(realPoints, allEvents, fallbackInsulinEntries) {
        if (allEvents.isNotEmpty()) DetailedChartItemsBuilder.buildInsulinEntries(realPoints, allEvents)
        else fallbackInsulinEntries
    }
    val foodEntries = remember(realPoints, allEvents, fallbackFoodEntries) {
        if (allEvents.isNotEmpty()) DetailedChartItemsBuilder.buildFoodEntries(realPoints, allEvents)
        else fallbackFoodEntries
    }
    val activityEntries = remember(allEvents, fallbackActivityEntries) {
        if (allEvents.isNotEmpty()) DetailedChartItemsBuilder.buildActivityEntries(allEvents)
        else fallbackActivityEntries
    }
    val pointLevels = remember(realPoints, historyStartDate) {
        ContinuousGlucosePointLevels(
            raw = realPoints,
            intermediate = realPoints.bucketAverages(historyStartDate, INTERMEDIATE_BUCKET_MINUTES),
            daily = realPoints.dailyAverages()
        )
    }
    val visibleRealPoints = remember(pointLevels, viewportStart, viewportEnd, historyStartDate) {
        pointLevels.raw.visibleIn(historyStartDate, viewportStart, viewportEnd)
    }
    val glucoseSettings = timelineModel?.glucoseLevelSettings ?: dailyGlucoseModel?.glucoseLevelSettings
    val statistics = remember(visibleRealPoints, glucoseSettings) {
        ContinuousStatistics.from(visibleRealPoints, glucoseSettings)
    }
    val dayStatuses = remember(allEvents, dailyGlucoseModel) {
        allEvents.filter { it.type is EventType.Glucose && it.value != null }
            .groupBy { it.additionTime.toLocalDate() }
            .mapValues { (_, events) ->
                val values = events.mapNotNull { it.value }
                when {
                    values.any { it >= 10.0 } -> DayGlycemicStatus.HIGH
                    values.any { it <= 3.9 } -> DayGlycemicStatus.LOW
                    else -> DayGlycemicStatus.NORM
                }
            }
    }

    Dialog(onDismissRequest = onBackClick, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val view = LocalView.current
        SideEffect {
            (view.parent as? DialogWindowProvider)?.window?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    window.attributes.layoutInDisplayCutoutMode =
                        android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
        }
        val insets = WindowInsets.safeDrawing.asPaddingValues()
        val leftInset = insets.calculateStartPadding(LayoutDirection.Ltr)
        val rightInset = insets.calculateEndPadding(LayoutDirection.Ltr)
        val topInset = insets.calculateTopPadding()
        val bottomInset = insets.calculateBottomPadding()
        val maxSideMargin = max(max(leftInset, rightInset), 32.dp)

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().background(ContinuousBackground)
        ) {
            val isShort = maxHeight < 420.dp
            val bottomHeight = if (isShort) 74.dp else 80.dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = maxSideMargin,
                        end = maxSideMargin,
                        top = max(topInset, if (isShort) 8.dp else 12.dp),
                        bottom = max(bottomInset, if (isShort) 8.dp else 12.dp)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth().height(22.dp)) {
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onBackClick).padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(R.drawable.ic_arrow_left), "Назад", tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Назад", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
                    }
                }
                Spacer(Modifier.height(if (isShort) 4.dp else 6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(13.dp))
                        .border(1.dp, ContinuousBorder, RoundedCornerShape(13.dp))
                        .background(Color.White)
                        .padding(start = 16.dp, top = 12.dp, end = 12.dp, bottom = 6.dp)
                ) {
                    Column(Modifier.fillMaxSize()) {
                        val periodTitle = when {
                            viewportDuration <= 1 * DAY_MINUTES -> "Дневная статистика"
                            viewportDuration <= 7 * DAY_MINUTES -> "Недельная статистика"
                            viewportDuration <= 14 * DAY_MINUTES -> "2х недельная статистика"
                            else -> "Месячная статистика"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = periodTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ContinuousPrimary
                            )
                            Row(
                                modifier = Modifier.padding(end = 36.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Количество измерений:", fontSize = 12.sp, color = ContinuousSecondary)
                                Spacer(Modifier.width(4.dp))
                                Text("${statistics.count}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ContinuousPrimary)
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ммоль/л", fontSize = 11.sp, color = ContinuousSecondary)
                            Text("хлебных ед./инсулин", fontSize = 11.sp, color = ContinuousSecondary, modifier = Modifier.padding(end = 36.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            ContinuousAxisLabels(Modifier.width(20.dp).fillMaxHeight().padding(bottom = 20.dp))
                            ContinuousTimelineGraph(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                origin = historyStartDate,
                                viewportStart = viewportStart,
                                viewportDuration = viewportDuration,
                                maxViewportDuration = maxViewportDuration,
                                timelineDuration = historyDuration,
                                pointLevels = pointLevels,
                                insulinEntries = if (insulinVisible) insulinEntries else emptyList(),
                                foodEntries = if (foodVisible) foodEntries else emptyList(),
                                activityEntries = if (activityVisible) activityEntries else emptyList(),
                                transparentBars = insulinVisible && foodVisible && activityVisible,
                                selectedPoint = selectedPoint,
                                onPointSelected = { selectedPoint = it },
                                onViewportChanged = ::updateViewport
                            )
                            ContinuousAxisLabels(Modifier.width(20.dp).fillMaxHeight().padding(bottom = 20.dp), textAlign = TextAlign.End)
                            Spacer(Modifier.width(8.dp))
                            Column(
                                modifier = Modifier.width(24.dp).fillMaxHeight().padding(bottom = 20.dp),
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_syringe_blue),
                                    contentDescription = "Инсулин",
                                    tint = if (insulinVisible) Color(0xFF2E7BE6) else Color(0xFFB0B3BA),
                                    modifier = Modifier.size(18.dp).clickable { insulinVisible = !insulinVisible }
                                )
                                Icon(
                                    painter = painterResource(R.drawable.ic_spoon_and_fork_orange),
                                    contentDescription = "Еда",
                                    tint = if (foodVisible) Color(0xFFEE9C17) else Color(0xFFB0B3BA),
                                    modifier = Modifier.size(18.dp).clickable { foodVisible = !foodVisible }
                                )
                                Icon(
                                    painter = painterResource(R.drawable.ic_walking_blue),
                                    contentDescription = "Активность",
                                    tint = if (activityVisible) Color(0xFF8B5CF6) else Color(0xFFB0B3BA),
                                    modifier = Modifier.size(18.dp).clickable { activityVisible = !activityVisible }
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 32.dp, top = 2.dp)
                        ) {
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .pointerInput(historyDuration, viewportDuration) {
                                        detectTapGestures { offset ->
                                            if (historyDuration > 0) {
                                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                                val targetMinute = (fraction * historyDuration).toLong()
                                                updateViewport(targetMinute - viewportDuration / 2L, viewportDuration)
                                            }
                                        }
                                    }
                                    .pointerInput(historyDuration, viewportDuration) {
                                        detectDragGestures { change, _ ->
                                            change.consume()
                                            if (historyDuration > 0) {
                                                val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                                val targetMinute = (fraction * historyDuration).toLong()
                                                updateViewport(targetMinute - viewportDuration / 2L, viewportDuration)
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.5.dp)
                                        .clip(RoundedCornerShape(0.75.dp))
                                        .background(Color(0xFFDCE1E5))
                                )
                                if (historyDuration > 0) {
                                    val fractionStart = (viewportStart.toFloat() / historyDuration).coerceIn(0f, 1f)
                                    val fractionWidth = (viewportDuration.toFloat() / historyDuration).coerceIn(0.02f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fractionWidth)
                                            .offset(x = (maxWidth * fractionStart))
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(2.5.dp))
                                            .border(1.dp, Color(0xFF878B93), RoundedCornerShape(2.5.dp))
                                            .background(Color.White)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${historyStartDate.dayOfMonth}.${String.format(Locale.US, "%02d", historyStartDate.monthValue)}.${historyStartDate.year}",
                                    fontSize = 10.sp,
                                    color = ContinuousSecondary,
                                    fontWeight = FontWeight.Normal
                                )
                                Text(
                                    text = "${today.dayOfMonth}.${String.format(Locale.US, "%02d", today.monthValue)}.${today.year}",
                                    fontSize = 10.sp,
                                    color = ContinuousSecondary,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(if (isShort) 4.dp else 8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(bottomHeight).clip(RoundedCornerShape(13.dp))
                        .border(1.dp, ContinuousBorder, RoundedCornerShape(13.dp)).background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    if (selectedPoint != null) ContinuousSelectedPointSummary(selectedPoint!!)
                    else ContinuousStatisticsSummary(viewportTitle, statistics, viewportDuration)
                }
            }
        }
    }

    if (isDatePickerVisible) {
        GlucoseDatePickerDialog(
            initialDate = viewportStartDate,
            dayStatuses = dayStatuses,
            minDate = historyStartDate,
            maxDate = today,
            onDismissRequest = { isDatePickerVisible = false },
            onDateRangeSelected = { date, _ ->
                val target = ChronoUnit.DAYS.between(historyStartDate, date) * DAY_MINUTES
                updateViewport(target - viewportDuration / 2L, viewportDuration)
                isDatePickerVisible = false
            }
        )
    }
}

@Composable
private fun ContinuousTimelineGraph(
    modifier: Modifier,
    origin: LocalDate,
    viewportStart: Long,
    viewportDuration: Long,
    maxViewportDuration: Long,
    timelineDuration: Long,
    pointLevels: ContinuousGlucosePointLevels,
    insulinEntries: List<DetailedInsulinEntry>,
    foodEntries: List<DetailedFoodEntry>,
    activityEntries: List<DetailedActivityEntry>,
    transparentBars: Boolean,
    selectedPoint: DetailedGlucosePoint?,
    onPointSelected: (DetailedGlucosePoint?) -> Unit,
    onViewportChanged: (Long, Long) -> Unit
) {
    val density = LocalDensity.current
    // The canvas tracks the fingers immediately. The expensive viewport-dependent
    // model and statistics are committed only after the gesture pauses.
    var visualViewportStart by remember { mutableStateOf(viewportStart) }
    var visualViewportDuration by remember { mutableStateOf(viewportDuration) }
    LaunchedEffect(viewportStart, viewportDuration) {
        if (visualViewportStart != viewportStart || visualViewportDuration != viewportDuration) {
            visualViewportStart = viewportStart
            visualViewportDuration = viewportDuration
        }
    }
    LaunchedEffect(visualViewportStart, visualViewportDuration) {
        delay(120)
        if (visualViewportStart != viewportStart || visualViewportDuration != viewportDuration) {
            onViewportChanged(visualViewportStart, visualViewportDuration)
        }
    }
    val labels = remember(visualViewportStart, visualViewportDuration, origin) {
        continuousTimeLabels(origin, visualViewportStart, visualViewportDuration)
    }
    val visualResolution = remember(visualViewportDuration) {
        continuousGlucoseResolution(visualViewportDuration)
    }
    val visualPoints = remember(pointLevels, visualViewportStart, visualViewportDuration, origin, visualResolution) {
        pointLevels.pointsFor(visualResolution).visibleIn(
            origin,
            visualViewportStart,
            visualViewportStart + visualViewportDuration
        )
    }
    val visualRealPoints = remember(pointLevels, visualViewportStart, visualViewportDuration, origin) {
        pointLevels.raw.visibleIn(origin, visualViewportStart, visualViewportStart + visualViewportDuration)
    }
    BoxWithConstraints(modifier) {
        val widthPx = with(density) { maxWidth.toPx() }
        val labelAreaWidth = maxWidth
        val hitRadius = with(density) { 24.dp.toPx() }
        Column(Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .pointerInput(visualRealPoints, visualViewportStart, visualViewportDuration) {
                        detectTapGestures { offset ->
                            if (visualResolution != ContinuousGlucoseResolution.RAW) return@detectTapGestures
                            val nearest = visualRealPoints.minByOrNull { point ->
                                abs(point.continuousMinute(origin) - (visualViewportStart + offset.x / widthPx * visualViewportDuration)).toFloat()
                            }
                            val distance = nearest?.let { point ->
                                abs(point.continuousMinute(origin) - (visualViewportStart + offset.x / widthPx * visualViewportDuration)) /
                                    visualViewportDuration.toFloat() * widthPx
                            } ?: Float.MAX_VALUE
                            onPointSelected(nearest?.takeIf { distance <= hitRadius && it != selectedPoint })
                        }
                    }
                    .pointerInput(widthPx) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val nextDuration = (visualViewportDuration / zoom).roundToLong()
                            val focusFraction = (centroid.x / widthPx).coerceIn(0f, 1f)
                            val focusMinute = visualViewportStart + (visualViewportDuration * focusFraction).roundToLong()
                            val startAfterZoom = focusMinute - (nextDuration * focusFraction).roundToLong()
                            val panMinutes = (pan.x / widthPx * nextDuration).roundToLong()
                            val safeDuration = nextDuration.coerceIn(MIN_VIEWPORT_MINUTES, maxViewportDuration)
                            visualViewportDuration = safeDuration
                            visualViewportStart = (startAfterZoom - panMinutes)
                                .coerceIn(0L, (timelineDuration - safeDuration).coerceAtLeast(0L))
                        }
                    }
            ) {
                val activityTrackHeight = 9.dp.toPx()
                val chartHeight = size.height - activityTrackHeight
                val xForMinute: (Long) -> Float = { minute ->
                    ((minute - visualViewportStart).toFloat() / visualViewportDuration * size.width)
                }
                val dash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
                drawLine(Color(0xFFDCE1E5), Offset(0f, 0f), Offset(0f, chartHeight), strokeWidth = 1.dp.toPx())
                (0..3).forEach { index ->
                    val y = index / 4f * chartHeight
                    drawLine(Color(0xFFDCE1E5), Offset(0f, y), Offset(size.width, y), pathEffect = dash, strokeWidth = 1.dp.toPx())
                }
                drawLine(Color(0xFFDCE1E5), Offset(0f, chartHeight), Offset(size.width, chartHeight), strokeWidth = 1.dp.toPx())
                val events = continuousEvents(foodEntries, insulinEntries, origin, visualViewportStart, visualViewportDuration, size.width, density.density)
                events.forEach { event ->
                    val barHeight = chartHeight * event.height.coerceIn(0f, 1f)
                    drawRoundRect(
                        color = event.color.copy(alpha = if (transparentBars) .45f else 1f),
                        topLeft = Offset(event.x - event.barWidth / 2f, chartHeight - barHeight),
                        size = Size(event.barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                    drawCircle(event.color, 2.5.dp.toPx(), Offset(event.x, chartHeight + 3.dp.toPx()))
                    if (visualResolution == ContinuousGlucoseResolution.RAW) {
                        drawContinuousEventLabel(event, chartHeight - barHeight)
                    }
                }
                val offsets = visualPoints.map { point -> point to Offset(
                    xForMinute(point.continuousMinute(origin)),
                    (chartHeight - (point.value / MAX_GLUCOSE).coerceIn(0f, 1f) * chartHeight)
                        .coerceIn(5.dp.toPx(), chartHeight - 5.dp.toPx())
                ) }
                offsets.zipWithNext().forEach { (first, second) ->
                    val maxGap = when (visualResolution) {
                        ContinuousGlucoseResolution.RAW -> 2L * 60L
                        ContinuousGlucoseResolution.INTERMEDIATE -> 6L * 60L
                        ContinuousGlucoseResolution.DAILY -> DAY_MINUTES
                    }
                    val gap = second.first.continuousMinute(origin) - first.first.continuousMinute(origin)
                    if (gap <= maxGap) {
                        drawLine(continuousPointColor(first.first.value), first.second, second.second,
                            strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                    }
                }
                offsets.forEach { (point, offset) ->
                    val isSelected = point == selectedPoint
                    drawCircle(Color.White, if (isSelected) 8.dp.toPx() else 4.2.dp.toPx(), offset)
                    drawCircle(continuousPointColor(point.value), if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(), offset)
                    if (isSelected) {
                        drawLine(ContinuousSecondary.copy(alpha = .7f), Offset(offset.x, 0f), Offset(offset.x, chartHeight),
                            pathEffect = dash, strokeWidth = 1.dp.toPx())
                    }
                }
                activityEntries.forEach { entry ->
                    val start = entry.continuousStartMinute(origin)
                    val end = entry.continuousEndMinute(origin)
                    if (end >= visualViewportStart && start <= visualViewportStart + visualViewportDuration) {
                        drawLine(ContinuousBackground, Offset(xForMinute(start), chartHeight + 7.dp.toPx()),
                            Offset(xForMinute(end).coerceAtLeast(xForMinute(start) + 18.dp.toPx()), chartHeight + 7.dp.toPx()),
                            strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(24.dp)) {
                labels.forEach { label ->
                    Text(label.text, fontSize = 11.sp, color = ContinuousSecondary, fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 3.dp).width(48.dp)
                            .offset(x = (labelAreaWidth * label.fraction - 18.dp).coerceIn(0.dp, labelAreaWidth - 48.dp)),
                        textAlign = TextAlign.Center)
                }
            }
        }
    }
}

private enum class ContinuousGlucoseResolution { RAW, INTERMEDIATE, DAILY }

private data class ContinuousGlucosePointLevels(
    val raw: List<DetailedGlucosePoint>,
    val intermediate: List<DetailedGlucosePoint>,
    val daily: List<DetailedGlucosePoint>
) {
    fun pointsFor(resolution: ContinuousGlucoseResolution): List<DetailedGlucosePoint> = when (resolution) {
        ContinuousGlucoseResolution.RAW -> raw
        ContinuousGlucoseResolution.INTERMEDIATE -> intermediate
        ContinuousGlucoseResolution.DAILY -> daily
    }
}

private fun continuousGlucoseResolution(duration: Long): ContinuousGlucoseResolution = when {
    duration <= RAW_DETAILS_MAX_MINUTES -> ContinuousGlucoseResolution.RAW
    duration <= INTERMEDIATE_DETAILS_MAX_MINUTES -> ContinuousGlucoseResolution.INTERMEDIATE
    else -> ContinuousGlucoseResolution.DAILY
}

private fun List<DetailedGlucosePoint>.visibleIn(
    origin: LocalDate,
    start: Long,
    end: Long
): List<DetailedGlucosePoint> = filter { it.continuousMinute(origin) in start..end }

private fun List<DetailedGlucosePoint>.bucketAverages(
    origin: LocalDate,
    bucketMinutes: Long
): List<DetailedGlucosePoint> = asSequence()
    .filter { it.date != null }
    .groupBy { it.continuousMinute(origin) / bucketMinutes }
    .toSortedMap()
    .map { (bucket, points) ->
        val middleMinute = bucket * bucketMinutes + bucketMinutes / 2L
        val date = origin.plusDays(middleMinute / DAY_MINUTES)
        val minuteOfDay = middleMinute % DAY_MINUTES
        DetailedGlucosePoint(
            timeLabel = String.format(Locale.US, "%02d:%02d", minuteOfDay / 60L, minuteOfDay % 60L),
            value = points.map { it.value }.average().toFloat(),
            date = date,
            trendText = "среднее за 3 часа"
        )
    }
    .toList()

private data class ContinuousTimelineLabel(val fraction: Float, val text: String)
private data class ContinuousEvent(
    val x: Float,
    val barWidth: Float,
    val labelCenterX: Float,
    val height: Float,
    val color: Color,
    val label: String
)

private fun continuousTimeLabels(origin: LocalDate, start: Long, duration: Long): List<ContinuousTimelineLabel> {
    val step = when {
        duration <= 2 * 60L -> 30L
        duration <= 12 * 60L -> 60L
        duration <= DAY_MINUTES -> 3 * 60L
        duration <= 7 * DAY_MINUTES -> DAY_MINUTES
        duration <= 14 * DAY_MINUTES -> 2 * DAY_MINUTES
        else -> 5 * DAY_MINUTES
    }
    val first = (start / step) * step
    return generateSequence(first) { it + step }.takeWhile { it <= start + duration }.map { minute ->
        val date = origin.plusDays(minute / DAY_MINUTES)
        val time = minute % DAY_MINUTES
        val text = if (duration <= DAY_MINUTES) String.format(Locale.US, "%02d:%02d", time / 60, time % 60)
        else "${String.format(Locale.US, "%02d", date.dayOfMonth)}.${String.format(Locale.US, "%02d", date.monthValue)}"
        ContinuousTimelineLabel(((minute - start).toFloat() / duration).coerceIn(0f, 1f), text)
    }.toList()
}

private fun continuousEvents(
    food: List<DetailedFoodEntry>,
    insulin: List<DetailedInsulinEntry>,
    origin: LocalDate,
    start: Long,
    duration: Long,
    width: Float,
    density: Float
): List<ContinuousEvent> {
    val foodByHour = food.groupBy { it.continuousMinute(origin) / 60L }
    val insulinByHour = insulin.groupBy { it.continuousMinute(origin) / 60L }
    val allHours = (foodByHour.keys + insulinByHour.keys).distinct().sorted()

    val result = mutableListOf<ContinuousEvent>()

    for (hour in allHours) {
        val eventMinute = hour * 60L + 30L
        if (eventMinute !in start..(start + duration)) continue

        val centerX = (eventMinute - start).toFloat() / duration * width
        val foodList = foodByHour[hour]
        val insulinList = insulinByHour[hour]

        val hasFood = !foodList.isNullOrEmpty()
        val hasInsulin = !insulinList.isNullOrEmpty()

        if (hasFood && hasInsulin) {
            val foodTotal = foodList!!.sumOf { it.continuousValue().toDouble() }.toFloat()
            val insulinTotal = insulinList!!.sumOf { it.continuousValue().toDouble() }.toFloat()

            val barW = 8.5f * density
            val gap = 3f * density
            val foodX = centerX - (barW / 2f + gap / 2f)
            val insulinX = centerX + (barW / 2f + gap / 2f)

            result.add(
                ContinuousEvent(
                    x = foodX,
                    barWidth = barW,
                    labelCenterX = foodX,
                    height = foodTotal / 150f,
                    color = ContinuousHigh,
                    label = "${String.format(Locale.US, "%.1f", foodTotal)} ХЕ"
                )
            )
            result.add(
                ContinuousEvent(
                    x = insulinX,
                    barWidth = barW,
                    labelCenterX = insulinX,
                    height = insulinTotal / 150f,
                    color = ContinuousNormal,
                    label = "${String.format(Locale.US, "%.1f", insulinTotal)} Ед."
                )
            )
        } else if (hasFood) {
            val foodTotal = foodList!!.sumOf { it.continuousValue().toDouble() }.toFloat()
            val barW = 14f * density
            result.add(
                ContinuousEvent(
                    x = centerX,
                    barWidth = barW,
                    labelCenterX = centerX,
                    height = foodTotal / 150f,
                    color = ContinuousHigh,
                    label = "${String.format(Locale.US, "%.1f", foodTotal)} ХЕ"
                )
            )
        } else if (hasInsulin) {
            val insulinTotal = insulinList!!.sumOf { it.continuousValue().toDouble() }.toFloat()
            val barW = 14f * density
            result.add(
                ContinuousEvent(
                    x = centerX,
                    barWidth = barW,
                    labelCenterX = centerX,
                    height = insulinTotal / 150f,
                    color = ContinuousNormal,
                    label = "${String.format(Locale.US, "%.1f", insulinTotal)} Ед."
                )
            )
        }
    }
    return result
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawContinuousEventLabel(event: ContinuousEvent, top: Float) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE; textSize = 10.sp.toPx(); textAlign = Paint.Align.CENTER }
    val width = paint.measureText(event.label) + 12.dp.toPx()
    val left = (event.labelCenterX - width / 2f).coerceIn(0f, size.width - width)
    val labelTop = (top - 22.dp.toPx()).coerceAtLeast(2.dp.toPx())
    drawRoundRect(
        color = event.color,
        topLeft = Offset(left, labelTop),
        size = Size(width, 18.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    drawContext.canvas.nativeCanvas.drawText(
        event.label,
        left + width / 2f,
        labelTop + 13.dp.toPx(),
        paint
    )
}

private data class ContinuousStatistics(
    val points: List<DetailedGlucosePoint>, val count: Int, val average: Float?, val normal: Int, val high: Int, val low: Int,
    val sd: Float?, val cv: Int?, val gmi: Float?
) {
    companion object {
        fun from(points: List<DetailedGlucosePoint>, settings: GlucoseLevelSettings?): ContinuousStatistics {
            val average = points.takeIf { it.isNotEmpty() }?.map { it.value }?.average()?.toFloat()
            val normal = points.count { settings?.normal?.contains(it.value.toDouble()) ?: (it.value in 3.9f..10f) }
            val high = points.count { settings?.high?.contains(it.value.toDouble()) ?: (it.value > 10f) }
            val low = points.count { settings?.low?.contains(it.value.toDouble()) ?: (it.value < 3.9f) }
            val sd = if (points.size >= 2) average?.let { mean -> sqrt(points.map { (it.value - mean) * (it.value - mean) }.average()).toFloat() } else null
            val cv = if (average != null && average > 0f && sd != null) (sd / average * 100).roundToLong().toInt() else null
            val gmi = average?.let { 12.71f + .091f * (it * 18.0182f) }
            return ContinuousStatistics(points, points.size, average, normal, high, low, sd, cv, gmi)
        }
    }
}

@Composable
private fun ContinuousAxisLabels(modifier: Modifier, textAlign: TextAlign = TextAlign.Start) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.SpaceBetween
) {
    listOf("16", "12", "8", "4", "0").forEach {
        Text(
            text = it,
            fontSize = 11.sp,
            color = ContinuousSecondary,
            fontWeight = FontWeight.Medium,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
@Composable private fun ContinuousEventAxis(modifier: Modifier) = Column(modifier, verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
    listOf("16", "12", "8", "4", "0").forEach { Text(it, fontSize = 11.sp, color = ContinuousSecondary, fontWeight = FontWeight.Medium, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
}
@Composable private fun ContinuousLayerButton(icon: Int, active: Boolean, color: Color, onClick: () -> Unit) = Box(
    Modifier.size(32.dp).clip(CircleShape).background(if (active) color.copy(alpha = .16f) else Color(0xFFF3F4F6)).clickable(onClick = onClick), contentAlignment = Alignment.Center
) { Icon(painterResource(icon), "Переключить слой", tint = if (active) color else Color(0xFFB0B3BA), modifier = Modifier.size(18.dp)) }

@Composable private fun ContinuousLegendItem(color: Color, label: String) = Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(8.dp).clip(CircleShape).background(color))
    Spacer(Modifier.width(5.dp))
    Text(label, fontSize = 11.sp, color = ContinuousSecondary)
}

@Composable private fun ContinuousExtremes(statistics: ContinuousStatistics) {
    val min = statistics.points.minByOrNull { it.value }
    val max = statistics.points.maxByOrNull { it.value }
    if (min != null && max != null && min != max) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ContinuousExtremeChip("min ${continuousFormat(min.value)}", ContinuousLow)
            Spacer(Modifier.width(6.dp))
            ContinuousExtremeChip("max ${continuousFormat(max.value)}", ContinuousHigh)
        }
    }
}

@Composable private fun ContinuousExtremeChip(text: String, color: Color) = Text(
    text = text,
    color = Color.White,
    fontSize = 11.sp,
    fontWeight = FontWeight.SemiBold,
    modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(color).padding(horizontal = 7.dp, vertical = 3.dp)
)

@Composable
private fun ContinuousStatisticsSummary(
    title: String,
    s: ContinuousStatistics,
    viewportDuration: Long
) = Row(
    modifier = Modifier.fillMaxSize(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    // 1. Column 1: Date
    Box(
        modifier = Modifier.padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            color = ContinuousPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }

    // Divider 1
    Box(Modifier.width(1.dp).height(50.dp).background(Color(0xFFE3E7EB)))

    // 2. Column 2: Average Glucose (Orange)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 10.dp)
    ) {
        Text(
            text = continuousFormat(s.average),
            fontSize = 32.sp,
            color = Color(0xFFEE7300),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "ммоль/л",
            fontSize = 11.sp,
            color = Color(0xFFEE7300),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = if (viewportDuration <= DAY_MINUTES) "средний за день" else "средний за период",
            fontSize = 10.sp,
            color = ContinuousSecondary
        )
    }

    // Divider 2
    Box(Modifier.width(1.dp).height(50.dp).background(Color(0xFFE3E7EB)))

    // 3. Column 3: TIR (3 sub-columns)
    val isSingleDay = viewportDuration <= DAY_MINUTES
    val totalMins = if (isSingleDay) 24 * 60L else viewportDuration
    val normMins = if (s.count > 0) (s.normal.toFloat() / s.count * totalMins).toLong() else 0L
    val highMins = if (s.count > 0) (s.high.toFloat() / s.count * totalMins).toLong() else 0L
    val lowMins = if (s.count > 0) (s.low.toFloat() / s.count * totalMins).toLong() else 0L

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 6.dp)
    ) {
        ContinuousTirColumn(ContinuousNormal, percent(s.normal, s.count), formatDuration(normMins, isSingleDay))
        ContinuousTirColumn(ContinuousHigh, percent(s.high, s.count), formatDuration(highMins, isSingleDay))
        ContinuousTirColumn(ContinuousLow, percent(s.low, s.count), formatDuration(lowMins, isSingleDay))
    }

    // Divider 3
    Box(Modifier.width(1.dp).height(50.dp).background(Color(0xFFE3E7EB)))

    // 4. Column 4: Variability metrics (CV, SD, GMI)
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        ContinuousMetric(s.cv?.let { "$it%" } ?: "-", "", "CV")
        ContinuousMetric(s.sd?.let(::continuousFormat) ?: "-", "", "SD")
        ContinuousMetric(s.gmi?.let { "${continuousFormat(it)}%" } ?: "-", "", "GMI")
    }
}

@Composable
private fun ContinuousTirColumn(
    dotColor: Color,
    percentText: String,
    timeText: String
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(dotColor))
        Spacer(Modifier.width(4.dp))
        Text("TIR", fontSize = 11.sp, color = ContinuousSecondary)
    }
    Text(
        text = percentText,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = ContinuousPrimary
    )
    Text(
        text = timeText,
        fontSize = 11.sp,
        color = ContinuousSecondary
    )
}

private fun formatDuration(mins: Long, isSingleDay: Boolean): String {
    return if (isSingleDay) {
        val h = mins / 60
        val m = mins % 60
        if (h >= 24) "24ч" else "${h}ч ${String.format(Locale.US, "%02d", m)}м"
    } else {
        val days = mins / (24 * 60)
        val remainingHours = (mins % (24 * 60)) / 60
        val m = mins % 60
        when {
            days > 0 -> "${days}д ${remainingHours}ч"
            remainingHours > 0 -> "${remainingHours}ч ${m}м"
            else -> "${m}м"
        }
    }
}

@Composable private fun ContinuousMetric(value: String, subtitle: String, label: String) = Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(if (label.isBlank()) subtitle else label, fontSize = 11.sp, color = ContinuousSecondary); Text(value, fontSize = 20.sp, color = ContinuousPrimary, fontWeight = FontWeight.Bold); Text(if (label.isBlank()) "" else subtitle, fontSize = 10.sp, color = ContinuousSecondary) }
@Composable private fun ContinuousSelectedPointSummary(point: DetailedGlucosePoint) = Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
    ContinuousMetric(point.timeLabel, "время", "")
    ContinuousMetric(continuousFormat(point.value), "ммоль/л", "")
    ContinuousMetric("${point.trendValue}", point.trendText, "Тренд")
    ContinuousMetric(point.foodUnits ?: "-", point.foodTimeAgo ?: "", "Еда")
    ContinuousMetric(point.insulinUnits ?: "-", point.insulinTimeAgo ?: "", "Инсулин")
    ContinuousMetric(point.activityDuration ?: "-", point.activityTimeAgo ?: "", "Активность")
}

private val RUSSIAN_MONTHS = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря"
)
private fun percent(value: Int, total: Int) = if (total == 0) "-" else "${value * 100 / total}%"
private fun continuousFormatDateRange(start: LocalDate, end: LocalDate): String {
    val startMonth = RUSSIAN_MONTHS.getOrElse(start.monthValue - 1) { "" }
    val endMonth = RUSSIAN_MONTHS.getOrElse(end.monthValue - 1) { "" }
    return if (start == end) {
        "${start.dayOfMonth} $startMonth ${start.year}"
    } else {
        "${start.dayOfMonth} $startMonth ${start.year} -\n${end.dayOfMonth} $endMonth ${end.year}"
    }
}
private fun continuousFormat(value: Float?) = value?.let { String.format(Locale.US, "%.1f", it).replace('.', ',') } ?: "-"
private fun continuousPointColor(value: Float) = when { value <= 3.9f -> ContinuousLow; value >= 10f -> ContinuousHigh; else -> ContinuousNormal }
private fun DetailedGlucosePoint.continuousMinute(origin: LocalDate): Long = ChronoUnit.DAYS.between(origin, date ?: origin) * DAY_MINUTES + continuousTimeMinutes(timeLabel)
private fun DetailedInsulinEntry.continuousMinute(origin: LocalDate): Long = ChronoUnit.DAYS.between(origin, date ?: origin) * DAY_MINUTES + continuousTimeMinutes(timeLabel)
private fun DetailedFoodEntry.continuousMinute(origin: LocalDate): Long = ChronoUnit.DAYS.between(origin, date ?: origin) * DAY_MINUTES + continuousTimeMinutes(timeLabel)
private fun DetailedActivityEntry.continuousStartMinute(origin: LocalDate): Long = ChronoUnit.DAYS.between(origin, startDate ?: origin) * DAY_MINUTES + continuousTimeMinutes(startTimeLabel)
private fun DetailedActivityEntry.continuousEndMinute(origin: LocalDate): Long = ChronoUnit.DAYS.between(origin, endDate ?: startDate ?: origin) * DAY_MINUTES + continuousTimeMinutes(endTimeLabel)
private fun DetailedInsulinEntry.continuousValue() = value ?: units.continuousEventValue()
private fun DetailedFoodEntry.continuousValue() = value ?: breadUnits.continuousEventValue()
private fun String.continuousEventValue() = trim().substringBefore(' ').replace(',', '.').toFloatOrNull() ?: 0f
private fun continuousTimeMinutes(label: String): Long { val parts = label.split(":"); return (((parts.getOrNull(0)?.toLongOrNull() ?: 0L) * 60L) + (parts.getOrNull(1)?.toLongOrNull() ?: 0L)).coerceIn(0L, DAY_MINUTES) }
private fun String.toContinuousLocalDate(): LocalDate = runCatching { LocalDate.parse(this) }.getOrElse { LocalDate.now() }
private fun Context.continuousFindActivity(): Activity? { var current: Context = this; while (current is ContextWrapper) { if (current is Activity) return current; current = current.baseContext }; return null }
