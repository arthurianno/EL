package com.elta.android.presentation.features.main.records.ui.compose

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.utils.BackendSyncStatusStore
import com.elta.android.presentation.utils.SyncAttemptTimeStore
import com.nullgr.core.rx.RxBus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.YearMonth

/**
 * Production entry point. RxBus integration is kept at this boundary;
 * [GlucoseDashboardContent] is an independent, previewable Compose UI.
 */
@Composable
fun GlucoseDashboardScreen(
    uiState: GlucoseDashboardUiState,
    bus: RxBus? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncState = rememberDashboardSyncState(uiState.syncTimeText)
    var loadedEventsByMonth by remember {
        mutableStateOf(emptyMap<YearMonth, List<com.elta.android.domain.features.diary.events.model.EventV2>>())
    }
    var requestedMonths by remember { mutableStateOf(emptySet<YearMonth>()) }
    val detailedEvents = remember(uiState.detailedChartData.events, loadedEventsByMonth) {
        (uiState.detailedChartData.events + loadedEventsByMonth.values.flatten()).distinct()
    }

    LaunchedEffect(uiState.syncTimeText) {
        syncState.updateDisplayedTime(uiState.syncTimeText)
    }

    DisposableEffect(bus) {
        if (bus == null) return@DisposableEffect onDispose { }

        val syncDisposable = bus.events<Events.Sync>().subscribe { event ->
            syncState.handle(event, context, scope)
        }
        val networkDisposable = bus.events<Events.NetworkProblemTryLater>().subscribe {
            syncState.showMessage(scope, "Отсутствует подключение к сети", 4_000L)
        }
        val rangeDisposable = bus.events<Events.DetailedChartRangeLoaded>().subscribe { event ->
            val month = YearMonth.from(event.start)
            loadedEventsByMonth = loadedEventsByMonth + (month to event.events)
            requestedMonths = requestedMonths - month
        }

        onDispose {
            syncDisposable.dispose()
            networkDisposable.dispose()
            rangeDisposable.dispose()
        }
    }

    val paletteSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    ModalBottomSheetLayout(
        modifier = modifier.fillMaxSize(),
        sheetState = paletteSheetState,
        sheetElevation = 0.dp,
        sheetContent = {
            if (BuildConfig.DEBUG) {
                PaletteSelectionSheet { palette ->
                    NewDesignPaletteController.select(palette)
                    bus?.event(Events.NewDesignPaletteChanged)
                    scope.launch { paletteSheetState.hide() }
                }
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }
        }
    ) {
        GlucoseDashboardContent(
            uiState = uiState,
            syncState = syncState.asUiState(),
            detailedEvents = detailedEvents,
            onAction = { action ->
                when (action) {
                    GlucoseDashboardAction.RequestSync -> {
                        if (syncState.isSyncing) return@GlucoseDashboardContent
                        if (bus == null) {
                            syncState.showMessage(scope, "Синхронизация недоступна", 3_000L)
                        } else {
                            bus.event(Events.ManualGlucometerSyncRequested)
                        }
                    }
                    is GlucoseDashboardAction.RequestDetailedRange -> {
                        var month = YearMonth.from(action.start)
                        val lastMonth = YearMonth.from(action.end)
                        while (!month.isAfter(lastMonth)) {
                            if (month !in loadedEventsByMonth && month !in requestedMonths) {
                                requestedMonths = requestedMonths + month
                                bus?.event(
                                    Events.DetailedChartRangeRequested(month.atDay(1), month.atEndOfMonth())
                                )
                            }
                            month = month.plusMonths(1)
                        }
                    }
                    is GlucoseDashboardAction.SelectCategory -> Unit
                }
            },
            onDetailedChartClosed = {
                requestedMonths = emptySet()
                loadedEventsByMonth = emptyMap()
            },
            onDebugPaletteLongClick = if (BuildConfig.DEBUG) {
                { scope.launch { paletteSheetState.show() } }
            } else {
                null
            }
        )
    }
}

/** Stateless dashboard UI: state enters through [uiState], user intent leaves through [onAction]. */
@Composable
internal fun GlucoseDashboardContent(
    uiState: GlucoseDashboardUiState,
    syncState: DashboardSyncUiState,
    detailedEvents: List<com.elta.android.domain.features.diary.events.model.EventV2> = uiState.detailedChartData.events,
    modifier: Modifier = Modifier,
    onAction: (GlucoseDashboardAction) -> Unit = {},
    onDetailedChartClosed: () -> Unit = {},
    onDebugPaletteLongClick: (() -> Unit)? = null
) {
    var selectedCategory by rememberSaveable { mutableStateOf(DashboardCategories.first()) }
    var isTransitioningToDetailed by rememberSaveable { mutableStateOf(false) }
    var isDetailedChartVisible by rememberSaveable { mutableStateOf(false) }

    ProvideTextStyle(value = TextStyle(fontFamily = GlucoseDashboardGothamPro)) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val layout = calculateGlucoseDashboardLayout(
                screenWidth = maxWidth,
                availableContentHeight = maxHeight,
                isEmptyState = !uiState.hasMeasurements
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (uiState.isDarkTheme) GlucoseDashboardTheme.DarkBackground else Color.White)
                ) {
                    DashboardHeader(
                        uiState = uiState,
                        syncState = syncState,
                        selectedCategory = selectedCategory,
                        layout = layout,
                        onCategorySelected = { category ->
                            selectedCategory = category
                            onAction(GlucoseDashboardAction.SelectCategory(category))
                        },
                        onSyncClick = { onAction(GlucoseDashboardAction.RequestSync) },
                        onDebugPaletteLongClick = onDebugPaletteLongClick
                    )
                    GlucoseLineChartCard(
                        isDarkTheme = uiState.isDarkTheme,
                        points = uiState.chartPoints,
                        designScale = layout.horizontalScale,
                        cardHeight = layout.chartHeight,
                        showDetailHint = uiState.hasMeasurements,
                        emptyStateText = if (uiState.hasMeasurements) {
                            "Нет измерений за выбранный период"
                        } else {
                            "Здесь появятся ваши данные"
                        },
                        onChartClick = { isTransitioningToDetailed = true }
                    )
                }

                if (isTransitioningToDetailed) {
                    GlucoseChartTransitionOverlay(
                        isDarkTheme = uiState.isDarkTheme,
                        onAnimationFinished = {
                            isDetailedChartVisible = true
                            isTransitioningToDetailed = false
                        }
                    )
                }

                if (isDetailedChartVisible) {
                    DetailedGlucoseChartScreen(
                        onBackClick = {
                            isDetailedChartVisible = false
                            onDetailedChartClosed()
                        },
                        glucosePoints = uiState.detailedChartData.glucosePoints,
                        insulinEntries = uiState.detailedChartData.insulinEntries,
                        foodEntries = uiState.detailedChartData.foodEntries,
                        activityEntries = uiState.detailedChartData.activityEntries,
                        dailyGlucoseModel = uiState.detailedChartData.dailyGlucoseModel,
                        allEvents = detailedEvents,
                        onDateRangeSelected = { start, end ->
                            onAction(GlucoseDashboardAction.RequestDetailedRange(start, end))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    uiState: GlucoseDashboardUiState,
    syncState: DashboardSyncUiState,
    selectedCategory: String,
    layout: GlucoseDashboardLayout,
    onCategorySelected: (String) -> Unit,
    onSyncClick: () -> Unit,
    onDebugPaletteLongClick: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(layout.headerHeight)
            .background(GlucoseDashboardTheme.getHeaderGradient(uiState.glucoseState, uiState.isDarkTheme))
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Spacer(modifier = Modifier.height(layout.navigationTopSpacing))
            DashboardCategoryTabs(
                selectedCategory = selectedCategory,
                glucoseState = uiState.glucoseState,
                horizontalScale = layout.horizontalScale,
                onCategorySelected = onCategorySelected
            )
            Spacer(modifier = Modifier.height(layout.gaugeTopSpacing))

            if (uiState.hasMeasurements) {
                GlucoseRingGauge(
                    glucoseValue = uiState.glucoseValue,
                    deltaText = uiState.deltaText,
                    glucoseTrend = uiState.glucoseTrend,
                    tirPercentage = uiState.tirPercentage,
                    syncTimeText = syncState.displayedTime,
                    breadUnitsText = uiState.breadUnitsText,
                    insulinText = uiState.insulinText,
                    state = uiState.glucoseState,
                    statusText = syncState.statusMessage.orEmpty(),
                    isStatusVisible = syncState.statusMessage != null,
                    isSyncing = syncState.isSyncing,
                    ringSize = layout.ringSize,
                    ringTopOffset = layout.ringTopOffset,
                    lowerControlsExtraOffset = layout.lowerControlsExtraOffset,
                    onSyncClick = onSyncClick,
                    onStatePillLongClick = onDebugPaletteLongClick
                )
            } else {
                NoMeasurementsGlucoseGauge(
                    ringSize = layout.ringSize,
                    availableHeight = layout.headerHeight -
                        layout.navigationTopSpacing -
                        33.dp * layout.horizontalScale -
                        layout.gaugeTopSpacing,
                    state = uiState.glucoseState,
                    isSyncing = syncState.isSyncing,
                    statusText = syncState.statusMessage.orEmpty(),
                    isStatusVisible = syncState.statusMessage != null,
                    onSyncClick = onSyncClick
                )
            }
        }
    }
}

@Composable
private fun DashboardCategoryTabs(
    selectedCategory: String,
    glucoseState: GlucoseState,
    horizontalScale: Float,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp * horizontalScale),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(33.dp * horizontalScale)
                .clip(RoundedCornerShape(35.5.dp * horizontalScale))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(2.dp * horizontalScale)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                DashboardCategories.forEach { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(35.5.dp * horizontalScale))
                            .background(
                                if (isSelected) GlucoseDashboardTheme.IndicatorPillBackground else Color.Transparent
                            )
                            .clickable { onCategorySelected(category) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isSelected) {
                                GlucoseDashboardTheme.getSelectedTabTextColor(glucoseState)
                            } else {
                                GlucoseDashboardTheme.TabUnselectedText
                            }
                        )
                    }
                }
            }
        }
    }
}

private class DashboardSyncState(
    initialTime: String,
    isBackendSyncInProgress: Boolean
) {
    var displayedTime by mutableStateOf(initialTime)
        private set
    var statusMessage by mutableStateOf(
        if (isBackendSyncInProgress) "Подключение к серверу..." else null
    )
        private set
    var isSyncing by mutableStateOf(isBackendSyncInProgress)
        private set
    private var hideJob: Job? = null

    fun updateDisplayedTime(value: String) {
        if (!isSyncing) displayedTime = value
    }

    fun handle(event: Events.Sync, context: android.content.Context, scope: kotlinx.coroutines.CoroutineScope) {
        when (event) {
            is Events.Sync.Glucometer.Started -> start(
                scope,
                "Подключение к устройству...",
                "Ошибка синхронизации с устройством",
                context
            )
            is Events.Sync.Glucometer.Success,
            is Events.Sync.Glucometer.NoNewEvents,
            is Events.Sync.Glucometer.InvalidTime -> updateInProgress("Устройство синхронизировано")
            is Events.Sync.Glucometer.Error,
            is Events.Sync.Glucometer.ErrorWithMessage,
            is Events.Sync.Glucometer.Nothing -> showMessage(
                scope,
                "Ошибка синхронизации с устройством",
                4_000L
            )
            is Events.Sync.Server.Started -> startServer(scope, context)
            is Events.Sync.Server.Success -> completed(scope, "Успешная синхронизация с сервером")
            is Events.Sync.Server.Error,
            is Events.Sync.Server.ErrorWithMessage -> showMessage(
                scope,
                "Ошибка синхронизации с сервером",
                4_000L
            )
        }
    }

    fun showMessage(scope: kotlinx.coroutines.CoroutineScope, message: String, timeoutMillis: Long) {
        hideJob?.cancel()
        isSyncing = false
        statusMessage = message
        hideJob = scope.launch {
            delay(timeoutMillis)
            statusMessage = null
        }
    }

    fun asUiState(): DashboardSyncUiState = DashboardSyncUiState(
        displayedTime = displayedTime,
        statusMessage = statusMessage,
        isSyncing = isSyncing
    )

    private fun start(
        scope: kotlinx.coroutines.CoroutineScope,
        message: String,
        fallbackMessage: String,
        context: android.content.Context
    ) {
        hideJob?.cancel()
        displayedTime = SyncAttemptTimeStore.recordAttempt(context)
        isSyncing = true
        statusMessage = message
        hideJob = scope.launch {
            delay(SyncFallbackTimeoutMillis)
            if (isSyncing) showMessage(scope, fallbackMessage, 3_000L)
        }
    }

    private fun startServer(
        scope: kotlinx.coroutines.CoroutineScope,
        context: android.content.Context
    ) {
        hideJob?.cancel()
        if (!isSyncing) displayedTime = SyncAttemptTimeStore.recordAttempt(context)
        isSyncing = true
        statusMessage = "Подключение к серверу..."
    }

    private fun updateInProgress(message: String) {
        hideJob?.cancel()
        isSyncing = true
        statusMessage = message
    }

    private fun completed(scope: kotlinx.coroutines.CoroutineScope, message: String) {
        displayedTime = "Только что"
        showMessage(scope, message, 3_000L)
    }
}

@Composable
private fun rememberDashboardSyncState(initialTime: String): DashboardSyncState =
    remember {
        DashboardSyncState(
            initialTime = initialTime,
            isBackendSyncInProgress = BackendSyncStatusStore.isInProgress()
        )
    }

@Composable
private fun PaletteSelectionSheet(onPaletteSelected: (NewDesignPalette) -> Unit) {
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
                            if (isSelected) GlucoseDashboardTheme.NormalChartColor else Color(0xFFF1F3F5)
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

private val DashboardCategories = listOf("Глюкоза", "Давление", "Инсулин")
private const val SyncFallbackTimeoutMillis = 8_000L

private val PreviewChartPoints = listOf(
    GlucosePoint("06:00", 5.3f),
    GlucosePoint("09:00", 6.1f),
    GlucosePoint("12:00", 7.4f),
    GlucosePoint("15:00", 5.8f),
    GlucosePoint("18:00", 6.7f)
)

private val PopulatedPreviewState = GlucoseDashboardUiState(
    glucoseValue = "6,7",
    deltaText = "0,5",
    glucoseTrend = GlucoseTrend(GlucoseTrendDirection.UP, "0,5"),
    tirPercentage = "73%",
    syncTimeText = "Сегодня, 10:42",
    breadUnitsText = "2,5 ХЕ",
    insulinText = "4,0 Ед.",
    chartPoints = PreviewChartPoints,
    detailedChartData = DetailedChartData(glucosePoints = PreviewChartPoints.map {
        DetailedGlucosePoint(timeLabel = it.timeLabel, value = it.value)
    })
)

@Preview(name = "Данные — норма", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun GlucoseDashboardNormalPreview() {
    GlucoseDashboardContent(
        uiState = PopulatedPreviewState,
        syncState = DashboardSyncUiState(displayedTime = PopulatedPreviewState.syncTimeText)
    )
}

@Preview(name = "Данные — высокий уровень", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun GlucoseDashboardHighPreview() {
    GlucoseDashboardContent(
        uiState = PopulatedPreviewState.copy(
            glucoseValue = "12,4",
            glucoseState = GlucoseState.HIGH,
            isDarkTheme = true
        ),
        syncState = DashboardSyncUiState(displayedTime = "Только что")
    )
}

@Preview(name = "Данные — низкий уровень", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun GlucoseDashboardLowPreview() {
    GlucoseDashboardContent(
        uiState = PopulatedPreviewState.copy(
            glucoseValue = "3,2",
            glucoseState = GlucoseState.LOW,
            glucoseTrend = GlucoseTrend(GlucoseTrendDirection.DOWN, "0,8")
        ),
        syncState = DashboardSyncUiState(displayedTime = "Сегодня, 10:42")
    )
}

@Preview(name = "Нет измерений", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun GlucoseDashboardEmptyPreview() {
    GlucoseDashboardContent(
        uiState = GlucoseDashboardUiState(),
        syncState = DashboardSyncUiState(displayedTime = "Нет измерений")
    )
}

@Preview(name = "Ошибка синхронизации", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun GlucoseDashboardSyncErrorPreview() {
    GlucoseDashboardContent(
        uiState = PopulatedPreviewState,
        syncState = DashboardSyncUiState(
            displayedTime = "Сегодня, 10:42",
            statusMessage = "Устройство недоступно"
        )
    )
}

@Preview(name = "Синхронизация", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun GlucoseDashboardSyncPreview() {
    GlucoseDashboardContent(
        uiState = PopulatedPreviewState,
        syncState = DashboardSyncUiState(
            displayedTime = "Сегодня, 10:42",
            statusMessage = "Синхронизация с прибором...",
            isSyncing = true
        )
    )
}
