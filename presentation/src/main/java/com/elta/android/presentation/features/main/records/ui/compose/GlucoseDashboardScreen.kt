package com.elta.android.presentation.features.main.records.ui.compose

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.utils.SyncAttemptTimeStore
import com.nullgr.core.rx.RxBus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GlucoseDashboardScreen(
    bus: RxBus? = null,
    glucoseValue: String = "—",
    deltaText: String = "—",
    glucoseTrend: GlucoseTrend? = null,
    tirPercentage: String = "—",
    syncTimeText: String = "Нет измерений",
    breadUnitsText: String = "0,9 Ед.",
    insulinText: String = "0,1 ХЕ",
    initialGlucoseState: GlucoseState = GlucoseState.NORMAL,
    isDarkTheme: Boolean = false,
    dailyGlucoseModel: com.elta.android.domain.features.diary.home.model.DailyGlucoseModel? = null,
    allDayEvents: List<com.elta.android.domain.features.diary.events.model.EventV2> = emptyList(),
    onTabSelected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedCategoryTab by remember { mutableStateOf("Глюкоза") }
    val currentState = initialGlucoseState
    var statusText by remember { mutableStateOf("") }
    var isStatusVisible by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var displayedSyncTime by remember(syncTimeText) { mutableStateOf(syncTimeText) }
    var isDetailedChartVisible by rememberSaveable { mutableStateOf(false) }
    var isTransitioningToDetailed by remember { mutableStateOf(false) }
    var detailedChartEvents by remember { mutableStateOf(allDayEvents) }
    var detailedChartEventsByMonth by remember { mutableStateOf(emptyMap<org.threeten.bp.YearMonth, List<com.elta.android.domain.features.diary.events.model.EventV2>>()) }
    var requestedDetailedMonths by remember { mutableStateOf(emptySet<org.threeten.bp.YearMonth>()) }

    LaunchedEffect(allDayEvents) {
        if (detailedChartEventsByMonth.isEmpty()) {
            detailedChartEvents = allDayEvents
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val paletteSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    var hideJob by remember { mutableStateOf<Job?>(null) }

    fun showStatus(text: String, syncing: Boolean, autoHideMs: Long?) {
        hideJob?.cancel()
        statusText = text
        isSyncing = syncing
        isStatusVisible = true

        if (autoHideMs != null) {
            hideJob = coroutineScope.launch {
                delay(autoHideMs)
                isStatusVisible = false
                isSyncing = false
            }
        } else if (syncing) {
            // Fallback safety timeout: if sync is still active after 8 sec without completion, auto-hide!
            hideJob = coroutineScope.launch {
                delay(8000)
                statusText = "Устройство недоступно"
                isSyncing = false
                delay(3000)
                isStatusVisible = false
            }
        }
    }

    val categories = listOf("Глюкоза", "Давление", "Инсулин")
    // A formatted glucose value can be available before the day model is populated, for
    // example immediately after a refresh. Treat it as data so the empty state never
    // flashes over a valid reading.
    val hasMeasurements = remember(dailyGlucoseModel, glucoseValue) {
        dailyGlucoseModel?.hasEvents == true ||
            glucoseValue.replace(',', '.').toFloatOrNull() != null
    }

    if (bus != null) {
        DisposableEffect(bus) {
            val syncDisposable = bus.events<Events.Sync>().subscribe { event ->
                when (event) {
                    is Events.Sync.Glucometer.Started -> {
                        displayedSyncTime = SyncAttemptTimeStore.recordAttempt(context)
                        showStatus("Синхронизация с прибором...", syncing = true, autoHideMs = null)
                    }
                    is Events.Sync.Glucometer.Success -> {
                        displayedSyncTime = "Только что"
                        showStatus("Синхронизация с прибором завершена", syncing = false, autoHideMs = 3000L)
                    }
                    is Events.Sync.Glucometer.NoNewEvents -> {
                        displayedSyncTime = "Только что"
                        showStatus("Нет новых измерений", syncing = false, autoHideMs = 3000L)
                    }
                    is Events.Sync.Glucometer.Error,
                    is Events.Sync.Glucometer.ErrorWithMessage -> {
                        showStatus("Устройство недоступно", syncing = false, autoHideMs = 4000L)
                    }
                    is Events.Sync.Server.Started -> {
                        displayedSyncTime = SyncAttemptTimeStore.recordAttempt(context)
                        showStatus("Синхронизация с сервером...", syncing = true, autoHideMs = null)
                    }
                    is Events.Sync.Server.Success -> {
                        displayedSyncTime = "Только что"
                        showStatus("Синхронизация с сервером завершена", syncing = false, autoHideMs = 3000L)
                    }
                    is Events.Sync.Server.Error,
                    is Events.Sync.Server.ErrorWithMessage -> {
                        showStatus("Сервер недоступен", syncing = false, autoHideMs = 4000L)
                    }
                    is Events.Sync.Glucometer.Nothing,
                    is Events.Sync.Glucometer.InvalidTime -> {
                        showStatus("Синхронизация завершена", syncing = false, autoHideMs = 2000L)
                    }
                }
            }

            val netDisposable = bus.events<Events.NetworkProblemTryLater>().subscribe {
                showStatus("Отсутствует подключение к сети", syncing = false, autoHideMs = 4000L)
            }
            val detailedChartDisposable = bus.events<Events.DetailedChartRangeLoaded>().subscribe { event ->
                val month = org.threeten.bp.YearMonth.from(event.start)
                detailedChartEventsByMonth = detailedChartEventsByMonth + (month to event.events)
                requestedDetailedMonths = requestedDetailedMonths - month
                detailedChartEvents = detailedChartEventsByMonth.values.flatten()
            }

            onDispose {
                syncDisposable.dispose()
                netDisposable.dispose()
                detailedChartDisposable.dispose()
            }
        }
    }

    val screenBg = if (isDarkTheme) GlucoseDashboardTheme.DarkBackground else Color.White
    val selectedTabTextColor = GlucoseDashboardTheme.getSelectedTabTextColor(currentState)

    ProvideTextStyle(value = TextStyle(fontFamily = GlucoseDashboardGothamPro)) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
        val layout = calculateGlucoseDashboardLayout(
            screenWidth = maxWidth,
            // The hosting container is measured above the app navigation and any system
            // navigation controls. This keeps the 60/40 layout inside the visible viewport.
            availableContentHeight = maxHeight,
            isEmptyState = !hasMeasurements
        )

        ModalBottomSheetLayout(
            modifier = Modifier.fillMaxSize(),
            sheetState = paletteSheetState,
            sheetContent = {
                if (BuildConfig.DEBUG) {
                    PaletteSelectionSheet(
                        onPaletteSelected = { palette ->
                            NewDesignPaletteController.select(palette)
                            bus?.event(Events.NewDesignPaletteChanged)
                            coroutineScope.launch { paletteSheetState.hide() }
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.height(1.dp))
                }
            }
        ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBg)
        ) {

            // Gradient Header Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(layout.headerHeight)
                    .background(GlucoseDashboardTheme.getHeaderGradient(currentState, isDarkTheme))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(layout.navigationTopSpacing + 10.dp))

                    // Top Category Pill Tabs Switcher ("Глюкоза", "Давление", "Инсулин")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp * layout.horizontalScale),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(33.dp * layout.horizontalScale)
                                .clip(RoundedCornerShape(35.5.dp * layout.horizontalScale))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(2.dp * layout.horizontalScale)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                categories.forEach { category ->
                                    val isSelected = category == selectedCategoryTab
                                    Box(
                                        modifier = Modifier
                                            .weight(3f)
                                            .height(33.dp * layout.horizontalScale)
                                            .clip(RoundedCornerShape(35.5.dp * layout.horizontalScale))
                                            .background(
                                                if (isSelected) {
                                                    GlucoseDashboardTheme.IndicatorPillBackground
                                                } else {
                                                    Color.Transparent
                                                }
                                            )
                                            .clickable {
                                                selectedCategoryTab = category
                                                onTabSelected(category)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = category,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = if (isSelected) selectedTabTextColor else GlucoseDashboardTheme.TabUnselectedText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(layout.gaugeTopSpacing))

                    val onSyncClick = {
                        if (!isSyncing) {
                            bus?.event(Events.ServerSyncRequested)
                                ?: showStatus("Синхронизация недоступна", syncing = false, autoHideMs = 3000L)
                        }
                    }

                    if (hasMeasurements) {
                        // Central Circular Ring Gauge Widget (Scales 100% dynamically with screenWidthDp)
                        GlucoseRingGauge(
                            glucoseValue = glucoseValue,
                            glucoseUnit = "ммоль/л",
                            deltaText = deltaText,
                            glucoseTrend = glucoseTrend,
                            tirPercentage = tirPercentage,
                            syncTimeText = displayedSyncTime,
                            breadUnitsText = breadUnitsText,
                            insulinText = insulinText,
                            state = currentState,
                            statusText = statusText,
                            isStatusVisible = isStatusVisible,
                            isSyncing = isSyncing,
                            ringSize = layout.ringSize,
                            ringTopOffset = layout.ringTopOffset,
                            lowerControlsExtraOffset = layout.lowerControlsExtraOffset,
                            onSyncClick = onSyncClick,
                            onStatePillLongClick = if (BuildConfig.DEBUG) {
                                { coroutineScope.launch { paletteSheetState.show() } }
                            } else null
                        )
                    } else {
                        NoMeasurementsGlucoseGauge(
                            ringSize = layout.ringSize,
                            availableHeight = layout.headerHeight -
                                layout.navigationTopSpacing -
                                33.dp * layout.horizontalScale -
                                layout.gaugeTopSpacing,
                            state = currentState,
                            isSyncing = isSyncing,
                            statusText = statusText,
                            isStatusVisible = isStatusVisible,
                            onSyncClick = onSyncClick
                        )
                    }

                }
            }

            val chartPoints = remember(dailyGlucoseModel, allDayEvents) {
                val pointsFromModel = dailyGlucoseModel?.let {
                    com.elta.android.presentation.features.main.records.mapper.DetailedChartItemsBuilder.buildPoints(it, allDayEvents)
                }
                if (!pointsFromModel.isNullOrEmpty()) {
                    pointsFromModel.map { GlucosePoint(it.timeLabel, it.value) }
                } else emptyList()
            }

            // Lower Chart Card Section
            GlucoseLineChartCard(
                isDarkTheme = isDarkTheme,
                points = chartPoints,
                designScale = layout.horizontalScale,
                cardHeight = layout.chartHeight,
                emptyStateText = if (hasMeasurements) {
                    "Нет измерений за выбранный период"
                } else {
                    "Здесь появятся ваши данные"
                },
                onChartClick = {
                    isTransitioningToDetailed = true
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ChartInteractionHint(
                isDarkTheme = isDarkTheme,
                designScale = layout.horizontalScale,
                onClick = {
                    isTransitioningToDetailed = true
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

        }

            if (isTransitioningToDetailed) {
            GlucoseChartTransitionOverlay(
                isDarkTheme = isDarkTheme,
                onAnimationFinished = {
                    isDetailedChartVisible = true
                    isTransitioningToDetailed = false
                }
            )
        }

            if (isDetailedChartVisible) {
            val realPoints = remember(dailyGlucoseModel, allDayEvents) {
                dailyGlucoseModel?.let { com.elta.android.presentation.features.main.records.mapper.DetailedChartItemsBuilder.buildPoints(it, allDayEvents) }
            }
            val realInsulin = remember(realPoints, allDayEvents) {
                realPoints?.let { com.elta.android.presentation.features.main.records.mapper.DetailedChartItemsBuilder.buildInsulinEntries(it, allDayEvents) }
            }
            val realFood = remember(realPoints, allDayEvents) {
                realPoints?.let { com.elta.android.presentation.features.main.records.mapper.DetailedChartItemsBuilder.buildFoodEntries(it, allDayEvents) }
            }
            val realActivity = remember(allDayEvents) {
                com.elta.android.presentation.features.main.records.mapper.DetailedChartItemsBuilder.buildActivityEntries(allDayEvents)
            }

            val todayDate = remember { getTodayFormattedDate() }

            DetailedGlucoseChartScreen(
                onBackClick = {
                    isDetailedChartVisible = false
                    requestedDetailedMonths = emptySet()
                    detailedChartEventsByMonth = emptyMap()
                    detailedChartEvents = allDayEvents
                },
                initialDate = todayDate,
                glucosePoints = realPoints ?: emptyList(),
                insulinEntries = realInsulin ?: emptyList(),
                foodEntries = realFood ?: emptyList(),
                activityEntries = realActivity ?: emptyList(),
                dailyGlucoseModel = dailyGlucoseModel,
                allEvents = detailedChartEvents,
                onDateRangeSelected = { start, end ->
                    var month = org.threeten.bp.YearMonth.from(start)
                    val lastMonth = org.threeten.bp.YearMonth.from(end)
                    while (!month.isAfter(lastMonth)) {
                        if (month !in detailedChartEventsByMonth && month !in requestedDetailedMonths) {
                            requestedDetailedMonths = requestedDetailedMonths + month
                            bus?.event(
                                Events.DetailedChartRangeRequested(
                                    month.atDay(1),
                                    month.atEndOfMonth()
                                )
                            )
                        }
                        month = month.plusMonths(1)
                    }
                }
            )
            }
        }
        }
        }
    }
}

@Composable
private fun PaletteSelectionSheet(
    onPaletteSelected: (NewDesignPalette) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlucoseDashboardTheme.LightCardBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Палитра интерфейса",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF353B4B)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NewDesignPalette.entries.forEach { palette ->
                val isSelected = NewDesignPaletteController.activePalette == palette
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (isSelected) GlucoseDashboardTheme.NormalChartColor
                            else Color(0xFFF1F3F5)
                        )
                        .clickable { onPaletteSelected(palette) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Палитра ${palette.name}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF353B4B)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartInteractionHint(
    isDarkTheme: Boolean,
    designScale: Float,
    onClick: () -> Unit = {}
) {
    val hintColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.62f)
    } else {
        Color(0xFF878B93)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(25.dp * designScale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_info_fill),
            contentDescription = null,
            tint = hintColor,
            modifier = Modifier.size(20.dp * designScale)
        )
        Spacer(modifier = Modifier.width(13.dp * designScale))
        Text(
            text = "Нажмите на график для большей статистики",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = hintColor
        )
    }
}
