package com.elta.android.presentation.features.main.records.ui.compose

import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.home.interactor.buildDailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.DailyGlucoseModel
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.records.mapper.DetailedChartItemsBuilder
import java.util.Locale
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.ChronoUnit

internal const val DAY_MINUTES = 24L * 60L
internal const val MIN_VIEWPORT_MINUTES = 60L
internal const val INITIAL_VIEWPORT_MINUTES = DAY_MINUTES
internal const val MAX_VIEWPORT_MINUTES = 30L * DAY_MINUTES
internal const val RAW_DETAILS_MAX_MINUTES = 2L * DAY_MINUTES
internal const val INTERMEDIATE_DETAILS_MAX_MINUTES = 6L * DAY_MINUTES
internal const val INTERMEDIATE_BUCKET_MINUTES = 3L * 60L
internal const val MAX_GLUCOSE = 16f
internal const val TREND_LINE_DASH_GAP_MINUTES = DAY_MINUTES

internal val ContinuousBackground get() = NewDesignPaletteController.colors.normalEnd
internal val ContinuousPrimary = Color(0xFF3D4556)
internal val ContinuousSecondary = Color(0xFF878B93)
internal val ContinuousBorder = Color(0xFFA4A4A4)
internal val ContinuousLow = Color(0xFFD93B17)
internal val ContinuousNormal = GlucoseDashboardTheme.NormalChartColor
internal val ContinuousHigh = Color(0xFFEE9C17)

internal enum class ContinuousTrendLineStyle {
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
    val chartPreferences = remember(context) { ContinuousChartPreferences(context) }
    var trendLineStyle by remember {
        mutableStateOf(chartPreferences.readTrendLineStyle())
    }
    var isTrendLineMenuVisible by remember { mutableStateOf(false) }
    var showZoomHint by remember {
        mutableStateOf(chartPreferences.shouldShowZoomHint())
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
    LaunchedEffect(viewportStartDate, viewportEndDate, allEvents) {
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
                                                chartPreferences.saveTrendLineStyle(trendLineStyle)
                                                isTrendLineMenuVisible = false
                                            }
                                        ) {
                                            Text(stringResource(R.string.glucose_trend_line_sharp))
                                        }
                                        DropdownMenuItem(
                                            onClick = {
                                                trendLineStyle = ContinuousTrendLineStyle.SMOOTH
                                                chartPreferences.saveTrendLineStyle(trendLineStyle)
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
                                            chartPreferences.markZoomHintShown()
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
