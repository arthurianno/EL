package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.features.main.records.mapper.chartPointsFor
import com.elta.android.presentation.features.main.records.mapper.detailedEventsFor
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

/** Dashboard UI receives state and reports user actions to its host. */
@Composable
fun GlucoseDashboardScreen(
    uiState: GlucoseDashboardUiState,
    loadedEventsByMonth: Map<YearMonth, List<EventV2>> = emptyMap(),
    syncState: DashboardSyncUiState,
    onAction: (GlucoseDashboardAction) -> Unit,
    onDebugPaletteSelected: (NewDesignPalette) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var selectedChartDate by remember { mutableStateOf(LocalDate.now()) }
    val detailedEvents = remember(uiState.detailedChartData.events, loadedEventsByMonth) {
        detailedEventsFor(uiState, loadedEventsByMonth)
    }
    val chartPoints = remember(selectedChartDate, uiState.chartPoints, uiState.detailedChartData.dailyGlucoseModel, loadedEventsByMonth) {
        chartPointsFor(selectedChartDate, uiState, loadedEventsByMonth)
    }

    LaunchedEffect(selectedChartDate, loadedEventsByMonth) {
        val month = YearMonth.from(selectedChartDate)
        if (selectedChartDate != LocalDate.now() && month !in loadedEventsByMonth) {
            onAction(GlucoseDashboardAction.RequestDetailedRange(month.atDay(1), month.atEndOfMonth()))
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
                    onDebugPaletteSelected(palette)
                    scope.launch { paletteSheetState.hide() }
                }
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }
        }
    ) {
        GlucoseDashboardContent(
            uiState = uiState,
            selectedChartDate = selectedChartDate,
            chartPoints = chartPoints,
            onChartDateSelected = { date ->
                selectedChartDate = date
            },
            syncState = syncState,
            detailedEvents = detailedEvents,
            onAction = onAction,
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
    selectedChartDate: LocalDate = LocalDate.now(),
    chartPoints: List<GlucosePoint> = uiState.chartPoints,
    onChartDateSelected: (LocalDate) -> Unit = {},
    syncState: DashboardSyncUiState,
    detailedEvents: List<EventV2> = uiState.detailedChartData.events,
    modifier: Modifier = Modifier,
    onAction: (GlucoseDashboardAction) -> Unit = {},
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
                DashboardViewport(
                    modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 600.dp).fillMaxSize()
                        .background(if (uiState.isDarkTheme) GlucoseDashboardTheme.DarkBackground else Color.White),
                    headerBackground = GlucoseDashboardTheme.getHeaderGradient(uiState.glucoseState, uiState.isDarkTheme),
                    chartInsets = (ChartCardTopInset + ChartCardBottomInset +
                        if (uiState.hasMeasurements) ChartDetailHintTopInset + ChartDetailHintHeight else 0.dp) * layout.horizontalScale,
                    header = {
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
                            onDebugPaletteLongClick = onDebugPaletteLongClick,
                            onAction = onAction
                        )
                    },
                    chart = { chartHeight ->
                        GlucoseLineChartCard(
                            isDarkTheme = uiState.isDarkTheme,
                            points = chartPoints,
                            selectedDate = selectedChartDate,
                            onDateSelected = onChartDateSelected,
                            designScale = layout.horizontalScale,
                            cardHeight = chartHeight,
                            showDetailHint = uiState.hasMeasurements,
                            emptyStateText = if (uiState.hasMeasurements) {
                                "Нет измерений за выбранный период"
                            } else {
                                "Здесь появятся ваши данные"
                            },
                            onChartClick = { isTransitioningToDetailed = true }
                        )
                    }
                )

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
                        initialDate = selectedChartDate.toString(),
                        onBackClick = {
                            isDetailedChartVisible = false
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

/** Measure the actual header, including the device panel, before assigning chart space. */
@Composable
private fun DashboardViewport(
    modifier: Modifier,
    chartInsets: Dp,
    headerBackground: Brush,
    header: @Composable () -> Unit,
    chart: @Composable (Dp) -> Unit
) {
    SubcomposeLayout(modifier) { constraints ->
        val contentConstraints = Constraints.fixedWidth(constraints.maxWidth)
        val headerPlaceable = subcompose("header", header).single().measure(contentConstraints)
        val availableChartHeight = (constraints.maxHeight - headerPlaceable.height).toDp() - chartInsets
        val chartPlaceable = subcompose("chart") {
            chart(availableChartHeight.coerceAtLeast(201.dp))
        }.single().measure(contentConstraints)
        val contentHeight = headerPlaceable.height + chartPlaceable.height
        // Extremely short windows and accessibility text still keep every control on screen.
        val scale = minOf(1f, constraints.maxHeight.toFloat() / contentHeight.coerceAtLeast(1))
        val left = ((constraints.maxWidth - constraints.maxWidth * scale) / 2f).toInt()
        val headerBackgroundPlaceable = subcompose("header-background") {
            Box(Modifier.background(headerBackground))
        }.single().measure(Constraints.fixed(constraints.maxWidth, (headerPlaceable.height * scale).toInt()))
        layout(constraints.maxWidth, constraints.maxHeight) {
            headerBackgroundPlaceable.place(0, 0)
            headerPlaceable.placeWithLayer(left, 0) {
                transformOrigin = TransformOrigin(0f, 0f)
                scaleX = scale
                scaleY = scale
            }
            chartPlaceable.placeWithLayer(left, (headerPlaceable.height * scale).toInt()) {
                transformOrigin = TransformOrigin(0f, 0f)
                scaleX = scale
                scaleY = scale
            }
        }
    }
}
