package com.elta.android.presentation.features.main.records.ui.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlinx.coroutines.launch
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.home.interactor.buildDailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.DailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.statistics.model.glucoseManagementIndicatorPercent
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
private const val TREND_LINE_DASH_GAP_MINUTES = DAY_MINUTES
private const val TREND_LINE_PREFERENCES = "glucose_chart_preferences"
private const val TREND_LINE_STYLE_KEY = "continuous_trend_line_style"
private const val ZOOM_HINT_SHOWN_KEY = "continuous_zoom_hint_shown"

private val ContinuousBackground get() = NewDesignPaletteController.colors.normalEnd
private val ContinuousPrimary = Color(0xFF3D4556)
private val ContinuousSecondary = Color(0xFF878B93)
private val ContinuousBorder = Color(0xFFA4A4A4)
private val ContinuousLow = Color(0xFFD93B17)
private val ContinuousNormal = GlucoseDashboardTheme.NormalChartColor
private val ContinuousHigh = Color(0xFFEE9C17)

private enum class ContinuousTrendLineStyle {
    SHARP,
    SMOOTH
}

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
    val context = LocalContext.current
    val activity = context.continuousFindActivity()
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
    var selectedEvent by remember { mutableStateOf<ContinuousEvent?>(null) }
    var insulinVisible by remember { mutableStateOf(true) }
    var foodVisible by remember { mutableStateOf(true) }
    var activityVisible by remember { mutableStateOf(true) }
    val trendLinePreferences = remember(context) {
        context.applicationContext.getSharedPreferences(TREND_LINE_PREFERENCES, Context.MODE_PRIVATE)
    }
    var trendLineStyle by remember {
        mutableStateOf(
            runCatching {
                ContinuousTrendLineStyle.valueOf(
                    trendLinePreferences.getString(TREND_LINE_STYLE_KEY, ContinuousTrendLineStyle.SHARP.name)
                        ?: ContinuousTrendLineStyle.SHARP.name
                )
            }.getOrDefault(ContinuousTrendLineStyle.SHARP)
        )
    }
    var isTrendLineMenuVisible by remember { mutableStateOf(false) }
    var showZoomHint by remember {
        mutableStateOf(!trendLinePreferences.getBoolean(ZOOM_HINT_SHOWN_KEY, false))
    }
    val maxViewportDuration = MAX_VIEWPORT_MINUTES.coerceAtMost(historyDuration)

    fun updateViewport(start: Long, duration: Long) {
        val safeDuration = duration.coerceIn(MIN_VIEWPORT_MINUTES.coerceAtMost(historyDuration), maxViewportDuration)
        viewportDuration = safeDuration
        viewportStart = start.coerceIn(0L, (historyDuration - safeDuration).coerceAtLeast(0L))
        selectedPoint = null
        selectedEvent = null
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
        val contentAlpha = remember { Animatable(0f) }
        val contentScale = remember { Animatable(0.96f) }

        LaunchedEffect(Unit) {
            launch {
                contentAlpha.animateTo(1f, tween(durationMillis = 320, easing = FastOutSlowInEasing))
            }
            launch {
                contentScale.animateTo(1f, tween(durationMillis = 320, easing = FastOutSlowInEasing))
            }
        }

        SideEffect {
            (view.parent as? DialogWindowProvider)?.window?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
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
            modifier = Modifier
                .fillMaxSize()
                .background(ContinuousBackground)
                .graphicsLayer {
                    alpha = contentAlpha.value
                    scaleX = contentScale.value
                    scaleY = contentScale.value
                }
        ) {
            val isShort = maxHeight < 420.dp
            val topPadding = max(topInset, if (isShort) 8.dp else 12.dp)
            val bottomPadding = max(bottomInset, if (isShort) 8.dp else 12.dp)
            val layout = continuousChartLayout(
                screenWidth = maxWidth,
                screenHeight = maxHeight,
                sideMargin = maxSideMargin,
                topPadding = topPadding,
                bottomPadding = bottomPadding,
                fontScale = LocalDensity.current.fontScale
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (layout.scrollContent) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                    .padding(
                        start = maxSideMargin,
                        end = maxSideMargin,
                        top = topPadding,
                        bottom = bottomPadding
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp)) {
                    Row(
                        modifier = Modifier.heightIn(min = 36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onBackClick)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(R.drawable.ic_arrow_left), "Назад", tint = Color.White, modifier = Modifier.size(20.dp).rotate(180f))
                        Spacer(Modifier.width(6.dp))
                        Text("Назад", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
                    }
                }
                Spacer(Modifier.height(if (isShort) 4.dp else 6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(layout.chartCardHeight)
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
                            modifier = Modifier.fillMaxWidth().heightIn(min = 22.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = periodTitle,
                                modifier = Modifier.weight(1f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ContinuousPrimary,
                                maxLines = 2,
                                lineHeight = 18.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(
                                    modifier = Modifier.padding(end = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Количество измерений:", fontSize = 12.sp, color = ContinuousSecondary)
                                    Spacer(Modifier.width(4.dp))
                                    Text("${statistics.count}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ContinuousPrimary)
                                }
                                if (layout.inlineLegend) {
                                    ContinuousLegend(Modifier.padding(end = 12.dp))
                                }
                                Box {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_chart_settings),
                                        contentDescription = stringResource(R.string.glucose_trend_line_settings),
                                        tint = ContinuousSecondary,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { isTrendLineMenuVisible = true }
                                    )
                                    DropdownMenu(
                                        expanded = isTrendLineMenuVisible,
                                        onDismissRequest = { isTrendLineMenuVisible = false }
                                    ) {
                                        DropdownMenuItem(
                                            onClick = {
                                                trendLineStyle = ContinuousTrendLineStyle.SHARP
                                                trendLinePreferences.edit()
                                                    .putString(TREND_LINE_STYLE_KEY, trendLineStyle.name)
                                                    .apply()
                                                isTrendLineMenuVisible = false
                                            }
                                        ) {
                                            Text(stringResource(R.string.glucose_trend_line_sharp))
                                        }
                                        DropdownMenuItem(
                                            onClick = {
                                                trendLineStyle = ContinuousTrendLineStyle.SMOOTH
                                                trendLinePreferences.edit()
                                                    .putString(TREND_LINE_STYLE_KEY, trendLineStyle.name)
                                                    .apply()
                                                isTrendLineMenuVisible = false
                                            }
                                        ) {
                                            Text(stringResource(R.string.glucose_trend_line_smooth))
                                        }
                                    }
                                }
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
                        if (!layout.inlineLegend) {
                            ContinuousLegend(Modifier.padding(start = 20.dp, top = 2.dp))
                            Spacer(Modifier.height(4.dp))
                        }
                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Row(modifier = Modifier.fillMaxSize()) {
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
                                    selectedPoint = selectedPoint,
                                    selectedEvent = selectedEvent,
                                    trendLineStyle = trendLineStyle,
                                    onPointSelected = { selectedPoint = it; selectedEvent = null },
                                    onEventSelected = { selectedEvent = it; selectedPoint = null },
                                    onViewportChanged = ::updateViewport
                                )
                                ContinuousAxisLabels(Modifier.width(20.dp).fillMaxHeight().padding(bottom = 20.dp), textAlign = TextAlign.End)
                                Spacer(Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier.width(32.dp).fillMaxHeight().padding(bottom = 20.dp),
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    ContinuousLayerButton(R.drawable.ic_syringe_blue, "Инсулин", insulinVisible, Color(0xFF2E7BE6)) {
                                        insulinVisible = !insulinVisible
                                        selectedEvent = null
                                    }
                                    ContinuousLayerButton(R.drawable.ic_spoon_and_fork_orange, "Еда", foodVisible, ContinuousHigh) {
                                        foodVisible = !foodVisible
                                        selectedEvent = null
                                    }
                                    ContinuousLayerButton(R.drawable.ic_walking_blue, "Активность", activityVisible, Color(0xFF8B5CF6)) {
                                        activityVisible = !activityVisible
                                        selectedEvent = null
                                    }
                                }
                            }
                            if (showZoomHint) {
                                Row(
                                    modifier = Modifier.align(Alignment.TopCenter)
                                        .padding(top = 8.dp)
                                        .fillMaxWidth(0.95f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ContinuousPrimary)
                                        .clickable {
                                            showZoomHint = false
                                            trendLinePreferences.edit().putBoolean(ZOOM_HINT_SHOWN_KEY, true).apply()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Сведите или разведите два пальца для изменения периода", modifier = Modifier.weight(1f), color = Color.White, fontSize = 11.sp, lineHeight = 12.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Понятно", color = Color(0xFF8FE5D8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 32.dp, top = 2.dp)
                        ) {
                            ContinuousViewportSlider(
                                start = viewportStart,
                                duration = viewportDuration,
                                timelineDuration = historyDuration,
                                onViewportChanged = ::updateViewport
                            )
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
                    modifier = Modifier.fillMaxWidth().height(layout.summaryHeight).clip(RoundedCornerShape(13.dp))
                        .border(1.dp, ContinuousBorder, RoundedCornerShape(13.dp)).background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    when {
                        selectedEvent != null -> ContinuousSelectedEventSummary(selectedEvent!!, historyStartDate)
                        selectedPoint != null -> ContinuousSelectedPointSummary(
                            selectedPoint!!, layout.compactSummary, layout.denseMetrics
                        )
                        else -> ContinuousStatisticsSummary(
                            viewportTitle, statistics, viewportDuration,
                            layout.compactSummary, layout.denseMetrics
                        )
                    }
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
private fun ContinuousViewportSlider(
    start: Long,
    duration: Long,
    timelineDuration: Long,
    onViewportChanged: (Long, Long) -> Unit
) {
    val currentStart by rememberUpdatedState(start)
    val currentDuration by rememberUpdatedState(duration)
    val currentUpdate by rememberUpdatedState(onViewportChanged)
    Canvas(
        modifier = Modifier.fillMaxWidth().height(28.dp)
            .pointerInput(timelineDuration) {
                fun seek(x: Float) {
                    val selectedDuration = currentDuration
                    currentUpdate(sliderViewportStart(x, size.width.toFloat(), selectedDuration, timelineDuration, 24.dp.toPx()), selectedDuration)
                }
                detectTapGestures { seek(it.x) }
            }
            .pointerInput(timelineDuration) {
                var dragGrabOffset = 0f
                fun seek(x: Float) {
                    val selectedDuration = currentDuration
                    currentUpdate(sliderViewportStart(x, size.width.toFloat(), selectedDuration, timelineDuration, 24.dp.toPx()), selectedDuration)
                }
                detectDragGestures(onDragStart = { touch ->
                    val selectedDuration = currentDuration
                    val knob = sliderKnobWidth(size.width.toFloat(), selectedDuration, timelineDuration, 24.dp.toPx())
                    val travel = (size.width - knob).coerceAtLeast(0f)
                    val currentLeft = currentStart.toFloat() / (timelineDuration - selectedDuration).coerceAtLeast(1L) * travel
                    dragGrabOffset = if (touch.x in currentLeft..(currentLeft + knob)) {
                        touch.x - currentLeft - knob / 2f
                    } else {
                        seek(touch.x)
                        0f
                    }
                }) { change, _ ->
                    seek(change.position.x - dragGrabOffset)
                    change.consume()
                }
            }
    ) {
        val middle = size.height / 2f
        drawLine(Color(0xFFDCE1E5), Offset(0f, middle), Offset(size.width, middle), strokeWidth = 2.dp.toPx())
        if (timelineDuration > 0) {
            val knob = sliderKnobWidth(size.width, duration, timelineDuration, 24.dp.toPx())
            val travel = (size.width - knob).coerceAtLeast(0f)
            val left = start.toFloat() / (timelineDuration - duration).coerceAtLeast(1L) * travel
            drawRoundRect(
                color = ContinuousPrimary,
                topLeft = Offset(left, middle - 5.dp.toPx()),
                size = Size(knob, 10.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
            )
        }
    }
}

internal fun sliderViewportStart(
    touchX: Float,
    width: Float,
    duration: Long,
    timelineDuration: Long,
    minimumKnobWidth: Float
): Long {
    if (width <= 0f || timelineDuration <= duration) return 0L
    val knob = sliderKnobWidth(width, duration, timelineDuration, minimumKnobWidth)
    val travel = (width - knob).coerceAtLeast(1f)
    val fraction = ((touchX - knob / 2f) / travel).coerceIn(0f, 1f)
    return ((timelineDuration - duration) * fraction).toLong()
}

private fun sliderKnobWidth(width: Float, duration: Long, timelineDuration: Long, minimum: Float): Float {
    if (width <= 0f || timelineDuration <= 0L) return 0f
    return (width * duration.toFloat() / timelineDuration)
        .coerceIn(minimum.coerceAtMost(width), width)
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
    selectedPoint: DetailedGlucosePoint?,
    selectedEvent: ContinuousEvent?,
    trendLineStyle: ContinuousTrendLineStyle,
    onPointSelected: (DetailedGlucosePoint?) -> Unit,
    onEventSelected: (ContinuousEvent?) -> Unit,
    onViewportChanged: (Long, Long) -> Unit
) {
    val density = LocalDensity.current
    // The canvas tracks the fingers immediately. The expensive viewport-dependent
    // model and statistics are committed only after the gesture pauses.
    var visualViewportStart by remember { mutableStateOf(viewportStart) }
    var visualViewportDuration by remember { mutableStateOf(viewportDuration) }
    val currentVisualStart by rememberUpdatedState(visualViewportStart)
    val currentVisualDuration by rememberUpdatedState(visualViewportDuration)
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
    val visibleEvents = remember(foodEntries, insulinEntries, activityEntries, origin, visualViewportStart, visualViewportDuration) {
        continuousEvents(foodEntries, insulinEntries, activityEntries, origin, visualViewportStart, visualViewportDuration)
    }
    BoxWithConstraints(modifier) {
        val widthPx = with(density) { maxWidth.toPx() }
        val labelAreaWidth = maxWidth
        val visibleLabels = remember(labels, maxWidth) {
            val minimumGap = 50f / maxWidth.value.coerceAtLeast(1f)
            val lastFraction = labels.lastOrNull()?.fraction ?: 1f
            buildList<ContinuousTimelineLabel> {
                labels.forEachIndexed { index, label ->
                    if (index == 0 || index == labels.lastIndex ||
                        (label.fraction - last().fraction >= minimumGap && lastFraction - label.fraction >= minimumGap)
                    ) add(label)
                }
            }
        }
        val hitRadius = with(density) { 24.dp.toPx() }
        val eventHitRadius = with(density) { 36.dp.toPx() }
        Column(Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .pointerInput(visualRealPoints, visibleEvents, visualViewportStart, visualViewportDuration, selectedPoint, selectedEvent) {
                        detectTapGestures { offset ->
                            val chartHeight = size.height - with(density) { 18.dp.toPx() }
                            val horizontalInset = with(density) { 7.dp.toPx() }.coerceAtMost(widthPx / 2f)
                            fun eventDistanceSquared(event: ContinuousEvent): Float {
                                val x = ((event.centerMinute - visualViewportStart).toFloat() / visualViewportDuration * widthPx +
                                    with(density) { event.horizontalOffsetDp.dp.toPx() }).coerceIn(horizontalInset, widthPx - horizontalInset)
                                val y = if (event.kind == ContinuousEventKind.ACTIVITY) chartHeight + with(density) { 8.dp.toPx() }
                                    else chartHeight - (chartHeight * event.height).coerceAtLeast(with(density) { 7.dp.toPx() }) - with(density) { 10.dp.toPx() }
                                return (x - offset.x) * (x - offset.x) + (y - offset.y) * (y - offset.y)
                            }
                            val nearestEvent = visibleEvents.minByOrNull(::eventDistanceSquared)
                            if (nearestEvent != null && eventDistanceSquared(nearestEvent) <= eventHitRadius * eventHitRadius) {
                                onEventSelected(nearestEvent.takeIf { it != selectedEvent })
                                return@detectTapGestures
                            }
                            if (visualResolution != ContinuousGlucoseResolution.RAW) return@detectTapGestures
                            val nearest = visualRealPoints.minByOrNull { point ->
                                val x = (point.continuousMinute(origin) - visualViewportStart).toFloat() / visualViewportDuration * widthPx
                                val y = chartHeight * (1f - (point.value / MAX_GLUCOSE).coerceIn(0f, 1f))
                                (x - offset.x) * (x - offset.x) + (y - offset.y) * (y - offset.y)
                            }
                            val hit = nearest?.takeIf { point ->
                                val x = (point.continuousMinute(origin) - visualViewportStart).toFloat() / visualViewportDuration * widthPx
                                val y = chartHeight * (1f - (point.value / MAX_GLUCOSE).coerceIn(0f, 1f))
                                (x - offset.x) * (x - offset.x) + (y - offset.y) * (y - offset.y) <= hitRadius * hitRadius
                            }
                            onPointSelected(hit?.takeIf { it != selectedPoint })
                        }
                    }
                    .pointerInput(widthPx) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val nextDuration = (currentVisualDuration / zoom).roundToLong()
                            val focusFraction = (centroid.x / widthPx).coerceIn(0f, 1f)
                            val focusMinute = currentVisualStart + (currentVisualDuration * focusFraction).roundToLong()
                            val startAfterZoom = focusMinute - (nextDuration * focusFraction).roundToLong()
                            val panMinutes = (pan.x / widthPx * nextDuration).roundToLong()
                            val safeDuration = nextDuration.coerceIn(MIN_VIEWPORT_MINUTES, maxViewportDuration)
                            visualViewportDuration = safeDuration
                            visualViewportStart = (startAfterZoom - panMinutes)
                                .coerceIn(0L, (timelineDuration - safeDuration).coerceAtLeast(0L))
                        }
                    }
            ) {
                val activityTrackHeight = 18.dp.toPx()
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
                visibleEvents.filter { it.kind != ContinuousEventKind.ACTIVITY }.forEach { event ->
                    val horizontalInset = minOf(7.dp.toPx(), size.width / 2f)
                    val x = (xForMinute(event.centerMinute) + event.horizontalOffsetDp.dp.toPx())
                        .coerceIn(horizontalInset, size.width - horizontalInset)
                    val barHeight = chartHeight * event.height.coerceIn(0f, 1f)
                    drawRoundRect(
                        color = event.color,
                        topLeft = Offset(x - 7.dp.toPx(), chartHeight - barHeight),
                        size = Size(14.dp.toPx(), barHeight.coerceAtLeast(7.dp.toPx())),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                    drawCircle(event.color, 3.dp.toPx(), Offset(x, chartHeight + 8.dp.toPx()))
                    if (visualResolution == ContinuousGlucoseResolution.RAW) {
                        drawContinuousEventLabel(event, x, chartHeight - barHeight)
                    }
                }
                val offsets = visualPoints.map { point -> point to Offset(
                    xForMinute(point.continuousMinute(origin)),
                    (chartHeight - (point.value / MAX_GLUCOSE).coerceIn(0f, 1f) * chartHeight)
                        .coerceIn(5.dp.toPx(), chartHeight - 5.dp.toPx())
                ) }
                offsets.zipWithNext().forEach { (first, second) ->
                    val gap = second.first.continuousMinute(origin) - first.first.continuousMinute(origin)
                    val lineColor = continuousPointColor(first.first.value)
                    val gapPathEffect = if (gap > TREND_LINE_DASH_GAP_MINUTES) {
                        PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()), 0f)
                    } else {
                        null
                    }
                    when (trendLineStyle) {
                        ContinuousTrendLineStyle.SHARP -> drawLine(
                            lineColor,
                            first.second,
                            second.second,
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = gapPathEffect
                        )

                        ContinuousTrendLineStyle.SMOOTH -> {
                            val middleX = (first.second.x + second.second.x) / 2f
                            val segment = Path().apply {
                                moveTo(first.second.x, first.second.y)
                                cubicTo(
                                    middleX,
                                    first.second.y,
                                    middleX,
                                    second.second.y,
                                    second.second.x,
                                    second.second.y
                                )
                            }
                            drawPath(
                                path = segment,
                                color = lineColor,
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    pathEffect = gapPathEffect
                                )
                            )
                        }
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
                visibleEvents.filter { it.kind == ContinuousEventKind.ACTIVITY }.forEach { event ->
                    val left = xForMinute(event.startMinute).coerceIn(0f, size.width)
                    val right = xForMinute(event.endMinute).coerceIn(0f, size.width)
                    drawLine(
                        color = event.color,
                        start = Offset(left, chartHeight + 8.dp.toPx()),
                        end = Offset((right).coerceAtLeast(left + 10.dp.toPx()).coerceAtMost(size.width), chartHeight + 8.dp.toPx()),
                        strokeWidth = 7.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
            Box(Modifier.fillMaxWidth().height(24.dp)) {
                visibleLabels.forEach { label ->
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
private enum class ContinuousEventKind { FOOD, INSULIN, ACTIVITY }

private data class ContinuousEvent(
    val kind: ContinuousEventKind,
    val startMinute: Long,
    val endMinute: Long,
    val centerMinute: Long,
    val height: Float,
    val color: Color,
    val label: String,
    val timeLabel: String,
    val horizontalOffsetDp: Float = 0f
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
    activity: List<DetailedActivityEntry>,
    origin: LocalDate,
    start: Long,
    duration: Long
): List<ContinuousEvent> = buildList {
    val end = start + duration
    val foodMinutes = food.map { it.continuousMinute(origin) }.toSet()
    val insulinMinutes = insulin.map { it.continuousMinute(origin) }.toSet()
    food.forEach { entry ->
        val minute = entry.continuousMinute(origin)
        if (minute in start..end) add(
            ContinuousEvent(
                kind = ContinuousEventKind.FOOD,
                startMinute = minute,
                endMinute = minute,
                centerMinute = minute,
                height = entry.heightRatio,
                color = ContinuousHigh,
                label = entry.breadUnits,
                timeLabel = entry.timeLabel,
                horizontalOffsetDp = if (minute in insulinMinutes) -32f else 0f
            )
        )
    }
    insulin.forEach { entry ->
        val minute = entry.continuousMinute(origin)
        if (minute in start..end) add(
            ContinuousEvent(
                kind = ContinuousEventKind.INSULIN,
                startMinute = minute,
                endMinute = minute,
                centerMinute = minute,
                height = entry.heightRatio,
                color = Color(0xFF2E7BE6),
                label = entry.units,
                timeLabel = entry.timeLabel,
                horizontalOffsetDp = if (minute in foodMinutes) 32f else 0f
            )
        )
    }
    activity.forEach { entry ->
        val eventStart = entry.continuousStartMinute(origin)
        val eventEnd = entry.continuousEndMinute(origin)
        if (eventEnd >= start && eventStart <= end) add(
            ContinuousEvent(
                kind = ContinuousEventKind.ACTIVITY,
                startMinute = eventStart,
                endMinute = eventEnd,
                centerMinute = (maxOf(eventStart, start) + minOf(eventEnd, end)) / 2,
                height = 0f,
                color = Color(0xFF8B5CF6),
                label = "${entry.durationMins} мин",
                timeLabel = entry.startTimeLabel
            )
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawContinuousEventLabel(
    event: ContinuousEvent,
    centerX: Float,
    top: Float
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 10.sp.toPx()
        textAlign = Paint.Align.CENTER
    }
    val badgeWidth = paint.measureText(event.label) + 12.dp.toPx()
    val left = (centerX - badgeWidth / 2f).coerceIn(0f, (size.width - badgeWidth).coerceAtLeast(0f))
    val labelTop = (top - 22.dp.toPx()).coerceAtLeast(2.dp.toPx())
    drawRoundRect(
        color = event.color,
        topLeft = Offset(left, labelTop),
        size = Size(badgeWidth, 18.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    drawContext.canvas.nativeCanvas.drawText(event.label, left + badgeWidth / 2f, labelTop + 13.dp.toPx(), paint)
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
            val gmi = average?.let { glucoseManagementIndicatorPercent(it.toDouble()).toFloat() }
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
@Composable
private fun ContinuousLayerButton(
    icon: Int,
    description: String,
    active: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(32.dp)
            .clip(CircleShape)
            .background(if (active) color.copy(alpha = 0.16f) else Color(0xFFF3F4F6))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = description,
            tint = if (active) color else Color(0xFFB0B3BA),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable private fun ContinuousLegendItem(color: Color, label: String) = Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(8.dp).clip(CircleShape).background(color))
    Spacer(Modifier.width(5.dp))
    Text(label, fontSize = 11.sp, color = ContinuousSecondary)
}

@Composable
private fun ContinuousLegend(modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ContinuousLegendItem(ContinuousLow, "Низкий")
        ContinuousLegendItem(ContinuousNormal, "Норма")
        ContinuousLegendItem(ContinuousHigh, "Высокий")
    }
}

@Composable
private fun ContinuousStatisticsSummary(
    title: String,
    statistics: ContinuousStatistics,
    viewportDuration: Long,
    compact: Boolean,
    denseMetrics: Boolean
) {
    val isSingleDay = viewportDuration <= DAY_MINUTES
    val totalMinutes = if (isSingleDay) DAY_MINUTES else viewportDuration
    val normalMinutes = if (statistics.count == 0) 0L else statistics.normal * totalMinutes / statistics.count
    val highMinutes = if (statistics.count == 0) 0L else statistics.high * totalMinutes / statistics.count
    val lowMinutes = if (statistics.count == 0) 0L else statistics.low * totalMinutes / statistics.count

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compactRowHeight = maxHeight / 2f
        if (compact) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth().heightIn(min = compactRowHeight), verticalAlignment = Alignment.CenterVertically) {
                    ContinuousDateMetric(title, Modifier.weight(1.1f))
                    ContinuousAverageMetric(statistics, isSingleDay, Modifier.weight(0.9f), compact = true)
                    ContinuousTirMetrics(statistics, normalMinutes, highMinutes, lowMinutes, isSingleDay, Modifier.weight(1.7f), compact = true)
                }
                ContinuousVariabilityMetrics(statistics, Modifier.fillMaxWidth().heightIn(min = compactRowHeight), compact = true)
            }
        } else {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                ContinuousDateMetric(title, Modifier.weight(1.1f))
                ContinuousAverageMetric(statistics, isSingleDay, Modifier.weight(0.9f), compact = denseMetrics)
                ContinuousTirMetrics(statistics, normalMinutes, highMinutes, lowMinutes, isSingleDay, Modifier.weight(1.6f), compact = denseMetrics)
                ContinuousVariabilityMetrics(statistics, Modifier.weight(1.3f), compact = denseMetrics)
            }
        }
    }
}

@Composable
private fun ContinuousDateMetric(title: String, modifier: Modifier) {
    Text(title, modifier = modifier, fontSize = 13.sp, lineHeight = 15.sp,
        color = ContinuousPrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
}

@Composable
private fun ContinuousAverageMetric(s: ContinuousStatistics, isSingleDay: Boolean, modifier: Modifier, compact: Boolean) {
    ContinuousMetric(
        value = continuousFormat(s.average),
        subtitle = if (isSingleDay) "средний за день" else "средний за период",
        label = "ммоль/л",
        modifier = modifier,
        color = Color(0xFFEE7300),
        compact = compact
    )
}

@Composable
private fun ContinuousTirMetrics(
    s: ContinuousStatistics,
    normalMinutes: Long,
    highMinutes: Long,
    lowMinutes: Long,
    isSingleDay: Boolean,
    modifier: Modifier,
    compact: Boolean
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        ContinuousTirColumn(ContinuousNormal, percent(s.normal, s.count), formatDuration(normalMinutes, isSingleDay), Modifier.weight(1f), compact)
        ContinuousTirColumn(ContinuousHigh, percent(s.high, s.count), formatDuration(highMinutes, isSingleDay), Modifier.weight(1f), compact)
        ContinuousTirColumn(ContinuousLow, percent(s.low, s.count), formatDuration(lowMinutes, isSingleDay), Modifier.weight(1f), compact)
    }
}

@Composable
private fun ContinuousVariabilityMetrics(s: ContinuousStatistics, modifier: Modifier, compact: Boolean) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        ContinuousMetric(s.cv?.let { "$it%" } ?: "-", "", "CV", Modifier.weight(1f), compact = compact)
        ContinuousMetric(s.sd?.let(::continuousFormat) ?: "-", "", "SD", Modifier.weight(1f), compact = compact)
        ContinuousMetric(s.gmi?.let { "${continuousFormat(it)}%" } ?: "-", "", "GMI", Modifier.weight(1f), compact = compact)
    }
}

@Composable
private fun ContinuousTirColumn(
    dotColor: Color,
    percentText: String,
    timeText: String,
    modifier: Modifier,
    compact: Boolean
) = Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(dotColor))
        Spacer(Modifier.width(3.dp))
        Text("TIR", fontSize = 10.sp, color = ContinuousSecondary)
    }
    Text(percentText, fontSize = if (compact) 16.sp else 20.sp,
        fontWeight = FontWeight.Bold, color = ContinuousPrimary, maxLines = 1)
    Text(timeText, fontSize = if (compact) 9.sp else 11.sp,
        color = ContinuousSecondary, maxLines = 1)
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

@Composable
private fun ContinuousMetric(
    value: String,
    subtitle: String,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = ContinuousPrimary,
    icon: Int? = null,
    compact: Boolean = false
) = Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(3.dp))
        }
        Text(label, fontSize = if (compact) 10.sp else 11.sp, color = ContinuousSecondary, maxLines = 1)
    }
    Text(value, fontSize = if (compact) 16.sp else 20.sp, color = color,
        fontWeight = FontWeight.Bold, maxLines = 1)
    if (subtitle.isNotEmpty()) Text(subtitle, fontSize = if (compact) 9.sp else 10.sp,
        color = ContinuousSecondary, maxLines = 2, lineHeight = if (compact) 10.sp else 11.sp)
}

@Composable
private fun ContinuousSelectedPointSummary(
    point: DetailedGlucosePoint,
    compact: Boolean,
    denseMetrics: Boolean
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compactRowHeight = maxHeight / 2f
        val trendColor = when {
            point.trendValue.startsWith("+") -> ContinuousHigh
            point.trendValue.startsWith("-") -> ContinuousLow
            else -> ContinuousNormal
        }
        if (compact) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth().heightIn(min = compactRowHeight), verticalAlignment = Alignment.CenterVertically) {
                    ContinuousMetric(point.timeLabel, "", "Время", Modifier.weight(1f), compact = true)
                    ContinuousMetric(continuousFormat(point.value), "ммоль/л", "Глюкоза", Modifier.weight(1f), continuousPointColor(point.value), compact = true)
                    ContinuousMetric(point.trendValue, point.trendText, "Тренд", Modifier.weight(1f), trendColor, compact = true)
                }
                Row(Modifier.fillMaxWidth().heightIn(min = compactRowHeight), verticalAlignment = Alignment.CenterVertically) {
                    ContinuousMetric(point.foodUnits ?: "-", point.foodTimeAgo ?: "", "Еда", Modifier.weight(1f), ContinuousHigh, R.drawable.ic_spoon_and_fork_orange, true)
                    ContinuousMetric(point.insulinUnits ?: "-", point.insulinTimeAgo ?: "", "Инсулин", Modifier.weight(1f), Color(0xFF2E7BE6), R.drawable.ic_syringe_blue, true)
                    ContinuousMetric(point.activityDuration ?: "-", point.activityTimeAgo ?: "", "Активность", Modifier.weight(1f), Color(0xFF8B5CF6), R.drawable.ic_walking_blue, true)
                }
            }
        } else {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                ContinuousMetric(point.timeLabel, "", "Время", Modifier.weight(1f), compact = denseMetrics)
                ContinuousMetric(continuousFormat(point.value), "ммоль/л", "Глюкоза", Modifier.weight(1f), continuousPointColor(point.value), compact = denseMetrics)
                ContinuousMetric(point.trendValue, point.trendText, "Тренд", Modifier.weight(1f), trendColor, compact = denseMetrics)
                ContinuousMetric(point.foodUnits ?: "-", point.foodTimeAgo ?: "", "Еда", Modifier.weight(1f), ContinuousHigh, R.drawable.ic_spoon_and_fork_orange, denseMetrics)
                ContinuousMetric(point.insulinUnits ?: "-", point.insulinTimeAgo ?: "", "Инсулин", Modifier.weight(1f), Color(0xFF2E7BE6), R.drawable.ic_syringe_blue, denseMetrics)
                ContinuousMetric(point.activityDuration ?: "-", point.activityTimeAgo ?: "", "Активность", Modifier.weight(1f), Color(0xFF8B5CF6), R.drawable.ic_walking_blue, denseMetrics)
            }
        }
    }
}

@Composable
private fun ContinuousSelectedEventSummary(event: ContinuousEvent, origin: LocalDate) {
    val title = when (event.kind) {
        ContinuousEventKind.FOOD -> "Еда"
        ContinuousEventKind.INSULIN -> "Инсулин"
        ContinuousEventKind.ACTIVITY -> "Активность"
    }
    val icon = when (event.kind) {
        ContinuousEventKind.FOOD -> R.drawable.ic_spoon_and_fork_orange
        ContinuousEventKind.INSULIN -> R.drawable.ic_syringe_blue
        ContinuousEventKind.ACTIVITY -> R.drawable.ic_walking_blue
    }
    val date = origin.plusDays(event.startMinute / DAY_MINUTES)
    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        ContinuousMetric(event.timeLabel, "${date.dayOfMonth}.${date.monthValue}", "Время", Modifier.weight(1f), compact = true)
        ContinuousMetric(title, "", "Событие", Modifier.weight(1f), event.color, icon, true)
        ContinuousMetric(event.label, "", "Значение", Modifier.weight(1f), event.color, compact = true)
    }
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
