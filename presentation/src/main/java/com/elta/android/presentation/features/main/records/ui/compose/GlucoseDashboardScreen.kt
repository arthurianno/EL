package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.bus.event
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
    syncTimeText: String = "5 часов назад",
    breadUnitsText: String = "0,9 Ед.",
    insulinText: String = "0,1 ХЕ",
    initialGlucoseState: GlucoseState = GlucoseState.NORMAL,
    isDarkTheme: Boolean = false,
    dailyGlucoseModel: com.elta.android.domain.features.diary.home.model.DailyGlucoseModel? = null,
    allDayEvents: List<com.elta.android.domain.features.diary.events.model.EventV2> = emptyList(),
    onTabSelected: (String) -> Unit = {}
) {
    var selectedCategoryTab by remember { mutableStateOf("Глюкоза") }
    var currentState by remember { mutableStateOf(initialGlucoseState) }
    var statusText by remember { mutableStateOf("") }
    var isStatusVisible by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var isDetailedChartVisible by rememberSaveable { mutableStateOf(false) }
    var detailedChartEvents by remember { mutableStateOf(allDayEvents) }
    var requestedDetailedRange by remember { mutableStateOf<Pair<org.threeten.bp.LocalDate, org.threeten.bp.LocalDate>?>(null) }

    LaunchedEffect(allDayEvents) {
        if (requestedDetailedRange == null) {
            detailedChartEvents = allDayEvents
        }
    }

    val coroutineScope = rememberCoroutineScope()
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

    if (bus != null) {
        DisposableEffect(bus) {
            val syncDisposable = bus.events<Events.Sync>().subscribe { event ->
                when (event) {
                    is Events.Sync.Glucometer.Started -> {
                        showStatus("Синхронизация с прибором...", syncing = true, autoHideMs = null)
                    }
                    is Events.Sync.Glucometer.Success -> {
                        showStatus("Синхронизация с прибором завершена", syncing = false, autoHideMs = 3000L)
                    }
                    is Events.Sync.Glucometer.NoNewEvents -> {
                        showStatus("Нет новых измерений", syncing = false, autoHideMs = 3000L)
                    }
                    is Events.Sync.Glucometer.Error,
                    is Events.Sync.Glucometer.ErrorWithMessage -> {
                        showStatus("Устройство недоступно", syncing = false, autoHideMs = 4000L)
                    }
                    is Events.Sync.Server.Started -> {
                        showStatus("Синхронизация с сервером...", syncing = true, autoHideMs = null)
                    }
                    is Events.Sync.Server.Success -> {
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
                if (requestedDetailedRange == (event.start to event.end)) {
                    detailedChartEvents = event.events
                }
            }

            onDispose {
                syncDisposable.dispose()
                netDisposable.dispose()
                detailedChartDisposable.dispose()
            }
        }
    }

    val screenBg = if (isDarkTheme) GlucoseDashboardTheme.DarkBackground else Color.White
    val selectedTabTextColor = GlucoseDashboardTheme.getMainTextColor(currentState)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(screenBg)
        ) {

            // Gradient Header Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlucoseDashboardTheme.getHeaderGradient(currentState, isDarkTheme))
                    .statusBarsPadding()
                    .padding(top = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Category Pill Tabs Switcher ("Глюкоза", "Давление", "Инсулин")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                categories.forEach { category ->
                                    val isSelected = category == selectedCategoryTab
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                if (isSelected) Color.White.copy(alpha = 0.65f) else Color.Transparent
                                            )
                                            .clickable {
                                                selectedCategoryTab = category
                                                onTabSelected(category)
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = category,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) selectedTabTextColor else GlucoseDashboardTheme.TabUnselectedText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Central Circular Ring Gauge Widget
                    GlucoseRingGauge(
                        glucoseValue = glucoseValue,
                        glucoseUnit = "ммоль/л",
                        deltaText = deltaText,
                        glucoseTrend = glucoseTrend,
                        tirPercentage = "73%",
                        syncTimeText = syncTimeText,
                        breadUnitsText = breadUnitsText,
                        insulinText = insulinText,
                        state = currentState,
                        statusText = statusText,
                        isStatusVisible = isStatusVisible,
                        isSyncing = isSyncing,
                        onSyncClick = {
                            if (isSyncing) {
                                showStatus("Синхронизация завершена", syncing = false, autoHideMs = 2000L)
                            } else {
                                showStatus("Синхронизация с прибором...", syncing = true, autoHideMs = null)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
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
                onChartClick = {
                    isDetailedChartVisible = true
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
                    requestedDetailedRange = null
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
                    requestedDetailedRange = start to end
                    bus?.event(Events.DetailedChartRangeRequested(start, end))
                }
            )
        }
    }
}
