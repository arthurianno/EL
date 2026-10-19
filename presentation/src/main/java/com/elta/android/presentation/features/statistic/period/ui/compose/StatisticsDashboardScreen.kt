@file:Suppress("LongMethod", "MagicNumber")

package com.elta.android.presentation.features.statistic.period.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.R
import com.elta.android.presentation.features.statistic.period.model.DEFAULT_VISIBLE_STATISTICS_BLOCKS
import com.elta.android.presentation.features.statistic.period.model.StatisticsBlock
import com.elta.android.presentation.features.statistic.period.ui.Period

internal val ScreenBackground = Color(0xFFF4F4F4)
internal val TextPrimary = Color(0xFF3D4556)
internal val TextSecondary = Color(0x8C3D4556)
internal val StatisticsHeader = Color(0xFF1FBFD2)
internal val Green = Color(0xFF3AE39D)
internal val GreenDark = Color(0xFF29AF99)
internal val Orange = Color(0xFFF3A30F)
internal val Red = Color(0xFFE6380E)
internal val Divider = Color(0xFFBBC0CA)
private val ContentMaxWidth = 480.dp
internal val CompactScreenWidth = 360.dp
// The Figma card starts shortly after the period picker. Keeping 171dp left a
// conspicuous empty header band on Android devices with a 24dp status inset.
private val DashboardSurfaceTop = 151.dp
private val HomeBottomNavigationHeight = 72.dp

@Composable
private fun Period.localizedName(): String = stringResource(
    when (this) {
        Period.SEVEN -> R.string.statistic_dashboard_last_seven_days
        Period.FOURTEEN -> R.string.statistic_dashboard_last_fourteen_days
        Period.THIRTY -> R.string.statistic_dashboard_last_thirty_days
        Period.NINETY -> R.string.statistic_dashboard_last_ninety_days
    }
)
@Composable
internal fun StatisticsDashboardScreen(
    uiState: StatisticsDashboardUiState,
    visibleBlocks: List<StatisticsBlock> = DEFAULT_VISIBLE_STATISTICS_BLOCKS,
    onVisibleBlocksSaved: (List<StatisticsBlock>) -> Unit = {},
    onPeriodSelected: (Period) -> Unit,
    onBack: () -> Unit,
    onExport: () -> Unit = {},
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.systemBars
) {
    var isDemoMode by rememberSaveable { mutableStateOf(false) }
    var showDemoModePicker by remember { mutableStateOf(false) }
    val displayedState = remember(uiState, isDemoMode) {
        if (BuildConfig.DEBUG && isDemoMode) uiState.toDemoDashboard() else uiState
    }
    var topBarHeightPx by remember { mutableIntStateOf(0) }
    val topBarHeight = with(LocalDensity.current) { topBarHeightPx.toDp() }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(StatisticsHeader)
            .then(
                if (BuildConfig.DEBUG) Modifier.pointerInput(Unit) {
                    detectTapGestures(onLongPress = { showDemoModePicker = true })
                } else Modifier
            )
    ) {
        val isCompact = maxWidth <= CompactScreenWidth
        var showSettings by remember { mutableStateOf(false) }
        val navigationBottomPadding = windowInsets
            .asPaddingValues()
            .calculateBottomPadding()
        StatisticsTopBar(
            uiState = displayedState,
            onPeriodSelected = onPeriodSelected,
            onBack = onBack,
            onExportClick = onExport,
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .onSizeChanged { topBarHeightPx = it.height },
            isCompact = isCompact,
            windowInsets = windowInsets
        )
        Surface(
            modifier = Modifier.padding(top = maxOf(DashboardSurfaceTop, topBarHeight + 16.dp)).fillMaxSize(),
            color = ScreenBackground,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = ContentMaxWidth)
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(
                        top = 0.dp,
                        // The host navigation overlays this Fragment. Keep the final items
                        // reachable above both the app navigation and the system gesture area.
                        bottom = 28.dp + HomeBottomNavigationHeight + navigationBottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        MainIndicatorsSection(
                            state = displayedState,
                            onSettingsClick = { showSettings = true }
                        )
                    }
                    statisticsSections(visibleBlocks).forEach { section ->
                        item(key = section) {
                            when (section) {
                                StatisticsSection.DISTRIBUTION -> DistributionSection(displayedState)
                                StatisticsSection.DAILY -> DailyVariationSection(displayedState)
                                StatisticsSection.EPISODES -> HypoHyperSection(displayedState)
                                StatisticsSection.KEY_METRICS -> KeyMetricsSection(displayedState)
                                StatisticsSection.COMPARISON -> PreviousPeriodSection(
                                    state = displayedState,
                                    isDemo = isDemoMode,
                                    onRequestDemoMode = {
                                        if (BuildConfig.DEBUG) showDemoModePicker = true
                                    }
                                )
                                StatisticsSection.ACTIVITY -> ActivitySection(displayedState)
                                StatisticsSection.FOOD -> NutritionSection(displayedState)
                            }
                        }
                    }
                }
            }
        }
        if (BuildConfig.DEBUG && showDemoModePicker) {
            DemoModePicker(
                isDemoMode = isDemoMode,
                onDemoModeSelected = { enabled ->
                    isDemoMode = enabled
                    showDemoModePicker = false
                },
                onDismiss = { showDemoModePicker = false }
            )
        }
        if (showSettings) {
            StatisticsSettingsDialog(
                visibleBlocks = visibleBlocks,
                onDismiss = { showSettings = false },
                onSave = { blocks ->
                    onVisibleBlocksSaved(blocks)
                    showSettings = false
                }
            )
        }
    }
}

@Composable
private fun StatisticsTopBar(
    uiState: StatisticsDashboardUiState,
    onPeriodSelected: (Period) -> Unit,
    onBack: () -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier,
    isCompact: Boolean,
    windowInsets: WindowInsets
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(windowInsets.asPaddingValues().calculateTopPadding()))
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material.Icon(
                painter = painterResource(R.drawable.ic_statistics_back),
                contentDescription = stringResource(R.string.content_description_back_button),
                tint = Color.White,
                modifier = Modifier.size(40.dp).clickable(onClick = onBack).padding(8.dp)
            )
            Text(
                text = stringResource(R.string.statistic_title),
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 17.sp
            )
            androidx.compose.material.Icon(
                painter = painterResource(R.drawable.ic_statistics_export),
                contentDescription = "Выгрузить статистику",
                tint = Color.White,
                modifier = Modifier.size(40.dp).clickable(onClick = onExportClick).padding(8.dp)
            )
        }
        Spacer(Modifier.height(30.dp))
        BoxWithConstraints(modifier = Modifier.padding(horizontal = 13.dp)) {
            val periodName = uiState.period.localizedName()
            val textSize = if (isCompact) 11.sp else 13.sp
            val textMeasurer = rememberTextMeasurer()
            val availableDateWidth = with(LocalDensity.current) { ((maxWidth - 42.dp) / 2).toPx() }
            val fullDateWidth = textMeasurer.measure(
                text = uiState.periodTitle,
                style = LocalTextStyle.current.copy(fontSize = textSize, fontWeight = FontWeight.Bold),
                maxLines = 1, softWrap = false
            ).size.width
            val periodTitle = (if (fullDateWidth > availableDateWidth) uiState.periodCompactTitle else uiState.periodTitle)
                .ifBlank { periodName }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(ScreenBackground)
                    .clickable { expanded = true }
                    .padding(horizontal = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = periodTitle,
                    modifier = Modifier.weight(1f),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = textSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = periodName,
                    modifier = Modifier.weight(1f),
                    color = TextPrimary,
                    fontSize = textSize,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                androidx.compose.material.Icon(
                    painter = painterResource(R.drawable.ic_arrow_down),
                    contentDescription = "Выбрать период",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                Period.values().forEach { period ->
                    DropdownMenuItem(onClick = { expanded = false; onPeriodSelected(period) }) {
                        Text(period.localizedName())
                    }
                }
            }
        }
    }
}
