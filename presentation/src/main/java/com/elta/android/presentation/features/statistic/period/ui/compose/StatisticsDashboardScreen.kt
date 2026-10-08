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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.records.ui.compose.NewDesignPaletteController
import com.elta.android.presentation.features.statistic.period.model.DEFAULT_VISIBLE_STATISTICS_BLOCKS
import com.elta.android.presentation.features.statistic.period.model.StatisticsBlock
import com.elta.android.presentation.features.statistic.period.ui.Period

internal val ScreenBackground = Color(0xFFF4F4F4)
internal val TextPrimary = Color(0xFF3D4556)
internal val TextSecondary = Color(0x8C3D4556)
internal val Green get() = NewDesignPaletteController.colors.normalStart
internal val GreenDark get() = NewDesignPaletteController.colors.normalEnd
internal val Orange get() = NewDesignPaletteController.colors.highStart
internal val Red get() = NewDesignPaletteController.colors.lowStart
internal val Divider = Color(0xFFBBC0CA)
private val ContentMaxWidth = 480.dp
internal val CompactScreenWidth = 360.dp
// The Figma card starts shortly after the period picker. Keeping 171dp left a
// conspicuous empty header band on Android devices with a 24dp status inset.
private val DashboardSurfaceTop = 151.dp
private val HomeBottomNavigationHeight = 72.dp
private fun String.compactForStatisticsHeader(isCompact: Boolean): String {
    if (!isCompact && length <= 18) return this
    return replace("января", "янв.")
        .replace("февраля", "февр.")
        .replace("марта", "мар.")
        .replace("апреля", "апр.")
        .replace("мая", "мая")
        .replace("июня", "июн.")
        .replace("июля", "июл.")
        .replace("августа", "авг.")
        .replace("сентября", "сент.")
        .replace("октября", "окт.")
        .replace("ноября", "нояб.")
        .replace("декабря", "дек.")
}

@Composable
internal fun StatisticsDashboardScreen(
    uiState: StatisticsDashboardUiState,
    visibleBlocks: List<StatisticsBlock> = DEFAULT_VISIBLE_STATISTICS_BLOCKS,
    onVisibleBlocksSaved: (List<StatisticsBlock>) -> Unit = {},
    onPeriodSelected: (Period) -> Unit,
    onBack: () -> Unit,
    onExport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isDemoMode by rememberSaveable { mutableStateOf(false) }
    var showDemoModePicker by remember { mutableStateOf(false) }
    val displayedState = remember(uiState, isDemoMode) {
        if (BuildConfig.DEBUG && isDemoMode) uiState.toDemoDashboard() else uiState
    }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(
                listOf(
                    NewDesignPaletteController.colors.normalStart,
                    NewDesignPaletteController.colors.normalEnd
                )
            ))
            .then(
                if (BuildConfig.DEBUG) Modifier.pointerInput(Unit) {
                    detectTapGestures(onLongPress = { showDemoModePicker = true })
                } else Modifier
            )
    ) {
        val isCompact = maxWidth <= CompactScreenWidth
        var showSettings by remember { mutableStateOf(false) }
        val navigationBottomPadding = WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding()
        StatisticsTopBar(
            uiState = displayedState,
            onPeriodSelected = onPeriodSelected,
            onBack = onBack,
            onExportClick = onExport,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = ContentMaxWidth)
                .align(Alignment.TopCenter),
            isCompact = isCompact
        )
        Surface(
            modifier = Modifier.padding(top = DashboardSurfaceTop).fillMaxSize(),
            color = ScreenBackground,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = ContentMaxWidth)
                        .align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(
                        top = 31.dp,
                        // The host navigation overlays this Fragment. Keep the final items
                        // reachable above both the app navigation and the system gesture area.
                        bottom = 28.dp + HomeBottomNavigationHeight + navigationBottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        MainIndicatorsSection(
                            state = displayedState,
                            isCompact = isCompact,
                            onSettingsClick = { showSettings = true }
                        )
                    }
                    visibleBlocks.forEach { block ->
                        item(key = block) {
                            when (block) {
                                StatisticsBlock.PERIOD -> Column {
                                    DistributionSection(displayedState)
                                    HypoHyperSection(displayedState)
                                }
                                StatisticsBlock.DAILY -> DailyVariationSection(displayedState)
                                StatisticsBlock.KEY_METRICS -> KeyMetricsSection(displayedState)
                                StatisticsBlock.COMPARISON -> PreviousPeriodSection(
                                    state = displayedState,
                                    isDemo = isDemoMode,
                                    onRequestDemoMode = {
                                        if (BuildConfig.DEBUG) showDemoModePicker = true
                                    }
                                )
                                StatisticsBlock.ACTIVITY,
                                StatisticsBlock.FOOD -> UnavailableStatisticsSection(block)
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
    isCompact: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material.Icon(
                painter = painterResource(R.drawable.ic_arrow_left),
                contentDescription = "Назад",
                tint = Color.White,
                modifier = Modifier.size(40.dp).clickable(onClick = onBack).padding(8.dp).rotate(180f)
            )
            Text(
                text = "Статистика",
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 17.sp
            )
            androidx.compose.material.Icon(
                painter = painterResource(R.drawable.ic_download_normal),
                contentDescription = "Выгрузить статистику",
                tint = Color.White,
                modifier = Modifier.size(40.dp).clickable(onClick = onExportClick).padding(8.dp)
            )
        }
        Box(modifier = Modifier.padding(horizontal = 13.dp)) {
            val periodTitle = uiState.periodTitle.compactForStatisticsHeader(isCompact)
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
                    fontSize = if (isCompact) 12.sp else 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = uiState.period.displayName,
                    modifier = Modifier.weight(1f),
                    color = TextPrimary,
                    fontSize = if (isCompact) 12.sp else 14.sp,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                androidx.compose.material.Icon(
                    painter = painterResource(R.drawable.ic_arrow_down),
                    contentDescription = "Выбрать период",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                Period.values().forEach { period ->
                    DropdownMenuItem(onClick = { expanded = false; onPeriodSelected(period) }) {
                        Text(period.displayName)
                    }
                }
            }
        }
    }
}
