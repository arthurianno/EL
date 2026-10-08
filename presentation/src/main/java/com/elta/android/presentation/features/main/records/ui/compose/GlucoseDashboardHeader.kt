package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DashboardHeader(
    uiState: GlucoseDashboardUiState,
    syncState: DashboardSyncUiState,
    selectedCategory: String,
    layout: GlucoseDashboardLayout,
    onCategorySelected: (String) -> Unit,
    onSyncClick: () -> Unit,
    onDebugPaletteLongClick: (() -> Unit)?,
    onAction: (GlucoseDashboardAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlucoseDashboardTheme.getHeaderGradient(uiState.glucoseState, uiState.isDarkTheme))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(layout.navigationTopSpacing))
            DashboardCategoryTabs(
                selectedCategory = selectedCategory,
                glucoseState = uiState.glucoseState,
                horizontalScale = layout.horizontalScale,
                onCategorySelected = onCategorySelected
            )
            Spacer(modifier = Modifier.height(layout.gaugeTopSpacing))

            if (LocalDensity.current.fontScale > 1.3f) {
                AccessibleGlucoseSummary(uiState)
            } else if (uiState.hasMeasurements) {
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
                    showSyncControl = false,
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
                    onSyncClick = onSyncClick,
                    showSyncControl = false
                )
            }
            DashboardDevicePanel(
                device = uiState.device,
                sensor = uiState.sensor,
                syncState = syncState,
                glucoseState = uiState.glucoseState,
                onSync = onSyncClick,
                onConnect = { onAction(GlucoseDashboardAction.ConnectDevice) },
                onDismissMessage = { onAction(GlucoseDashboardAction.DismissSyncMessage) },
                onRetry = { onAction(GlucoseDashboardAction.RetrySync(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
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
    val largeText = LocalDensity.current.fontScale > 1.3f
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp * horizontalScale)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .then(if (largeText) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DashboardCategories.forEach { category ->
            val selected = category == selectedCategory
            Box(
                modifier = (if (largeText) Modifier else Modifier.weight(1f))
                    .heightIn(min = 33.dp * horizontalScale)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (selected) GlucoseDashboardTheme.IndicatorPillBackground else Color.Transparent)
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    category, fontSize = 14.sp,
                    color = if (selected) GlucoseDashboardTheme.getSelectedTabTextColor(glucoseState)
                    else GlucoseDashboardTheme.TabUnselectedText,
                    maxLines = 1
                )
            }
        }
    }
}

/** The graphic gauge uses absolute coordinates; large system text needs a flowing summary. */
@Composable
private fun AccessibleGlucoseSummary(uiState: GlucoseDashboardUiState) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (!uiState.hasMeasurements) "Нет измерений" else when (uiState.glucoseState) {
                GlucoseState.NORMAL -> "Норма"
                GlucoseState.LOW -> "Низкий"
                GlucoseState.HIGH -> "Высокий"
            },
            color = Color.White, fontSize = 20.sp
        )
        Text(uiState.glucoseValue, color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        if (uiState.hasMeasurements) {
            Text("ммоль/л", color = Color.White, fontSize = 16.sp)
            uiState.glucoseTrend?.let { trend ->
                val direction = when (trend.direction) {
                    GlucoseTrendDirection.UP -> "↑"
                    GlucoseTrendDirection.DOWN -> "↓"
                    GlucoseTrendDirection.STABLE -> "→"
                }
                Text("$direction ${trend.valueText}", color = Color.White, fontSize = 16.sp)
            }
            listOf("TIR" to uiState.tirPercentage, "Хлебных ед." to uiState.breadUnitsText, "Инсулина" to uiState.insulinText)
                .forEach { (title, value) ->
                    Column(Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.13f), RoundedCornerShape(12.dp)).padding(12.dp)) {
                        Text(title, color = Color.White, fontSize = 14.sp)
                        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
        } else Text("Добавьте показатели вручную через «+» или синхронизируйте их с устройством", color = Color.White, fontSize = 16.sp)
    }
}

internal val DashboardCategories = listOf("Глюкоза", "Давление", "Инсулин")
