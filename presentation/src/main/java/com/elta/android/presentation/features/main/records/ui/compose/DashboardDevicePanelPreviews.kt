package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// These previews use the production panel. All device data and callbacks are local fixtures.
@Preview(name = "Ресурс датчика — три цвета", group = "Планки · ресурс", widthDp = 375, locale = "ru")
@Composable
private fun SensorResourcePanelsPreview() = PanelGallery {
    GlucoseState.entries.forEach { state ->
        PanelSample("Ресурс 52% · ${state.label}", state, detail = DashboardPanelDetail.SENSOR)
    }
}

@Preview(name = "Выбор синхронизации — три цвета", group = "Планки · выбор", widthDp = 375, locale = "ru")
@Composable
private fun SyncChoicePanelsPreview() = PanelGallery {
    GlucoseState.entries.forEach { state ->
        PanelSample("Выбор устройства · ${state.label}", state, detail = DashboardPanelDetail.SYNC_CHOICE)
    }
}

@Preview(name = "Устройства и ресурс", group = "Планки · состояния", widthDp = 375, locale = "ru")
@Composable
private fun DeviceSyncPanelsPreview() = PanelGallery {
    DeviceStates()
}

@Preview(name = "Узкий телефон · 320 dp", group = "Планки · адаптивность", widthDp = 320, locale = "ru")
@Preview(name = "Крупный шрифт · 200%", group = "Планки · адаптивность", widthDp = 375, fontScale = 2f, locale = "ru")
@Composable
private fun AdaptiveDevicePanelsPreview() = PanelGallery {
    PanelSample("Нет устройств", hasMeter = false, sensorPercent = null)
    PanelSample("Два устройства")
    PanelSample("Выбор синхронизации", detail = DashboardPanelDetail.SYNC_CHOICE)
    PanelSample("Ресурс датчика", detail = DashboardPanelDetail.SENSOR)
    PanelSample("Глюкометр", detail = DashboardPanelDetail.METER)
    PanelSample("Датчик не активен", sensorPercent = 0, sensorActive = false)
}

@Composable
private fun DeviceStates() {
    PanelSample("Нет устройств", hasMeter = false, sensorPercent = null)
    PanelSample("Только глюкометр", sensorPercent = null)
    PanelSample("Только датчик", hasMeter = false)
    PanelSample("Два устройства · 100%", sensorPercent = 100)
    PanelSample("Два устройства · 52%")
    PanelSample("Два устройства · 8%", sensorPercent = 8)
    PanelSample("Датчик не активен", sensorPercent = 0, sensorActive = false)
}

@Composable
private fun PanelGallery(content: @Composable () -> Unit) {
    MaterialTheme {
        ProvideTextStyle(TextStyle(fontFamily = GlucoseDashboardGothamPro)) {
            Column(
                Modifier.fillMaxWidth().background(Color(0xFFF1F2F4)).padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) { content() }
        }
    }
}

@Composable
private fun PanelSample(
    title: String,
    glucoseState: GlucoseState = GlucoseState.NORMAL,
    hasMeter: Boolean = true,
    sensorPercent: Int? = 52,
    sensorActive: Boolean = true,
    detail: DashboardPanelDetail? = null
) {
    val now = remember { System.currentTimeMillis() }
    val lastSync = now - 5 * 60 * 60 * 1_000L
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (title.isNotEmpty()) Text(title, Modifier.padding(horizontal = 16.dp), color = Color(0xFF353B4B), fontSize = 12.sp)
        DashboardDevicePanel(
            device = if (hasMeter) DashboardDevice("preview-meter", "Сателлит", null, lastSync - 60_000) else null,
            sensor = sensorPercent?.let { DashboardSensor("preview-sensor", it, 4, sensorActive, lastSync) },
            syncState = DashboardSyncUiState(displayedTime = ""),
            glucoseState = glucoseState,
            onSync = {}, onConnect = {}, onDismissMessage = {}, onRetry = {},
            initialDetail = detail,
            meterInfoSubtitle = "Серийный номер или последняя синхр.",
            modifier = Modifier.fillMaxWidth()
                .background(GlucoseDashboardTheme.getHeaderGradient(glucoseState))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

private val GlucoseState.label: String
    get() = when (this) {
        GlucoseState.NORMAL -> "норма"
        GlucoseState.HIGH -> "высокий"
        GlucoseState.LOW -> "низкий"
    }

// Separate previews correspond to the individual strips in the supplied design.
@Preview(name = "01 · Только глюкометр", group = "Макет · синхронизация", widthDp = 375, locale = "ru")
@Composable
private fun MeterSyncStripPreview() = SinglePanelPreview(sensorPercent = null)

@Preview(name = "02 · Только датчик", group = "Макет · синхронизация", widthDp = 375, locale = "ru")
@Composable
private fun SensorSyncStripPreview() = SinglePanelPreview(hasMeter = false)

@Preview(name = "03 · Датчик + глюкометр · полный ресурс", group = "Макет · синхронизация", widthDp = 375, locale = "ru")
@Composable
private fun BothDevicesFullStripPreview() = SinglePanelPreview(sensorPercent = 100)

@Preview(name = "04 · Датчик + глюкометр · половина ресурса", group = "Макет · синхронизация", widthDp = 375, locale = "ru")
@Composable
private fun BothDevicesHalfStripPreview() = SinglePanelPreview(sensorPercent = 52)

@Preview(name = "05 · Датчик + глюкометр · мало ресурса", group = "Макет · синхронизация", widthDp = 375, locale = "ru")
@Composable
private fun BothDevicesLowStripPreview() = SinglePanelPreview(sensorPercent = 8)

@Preview(name = "06 · Датчик не активен · плюс и крестик", group = "Макет · синхронизация", widthDp = 375, locale = "ru")
@Preview(name = "06 · Датчик не активен · 320 dp", group = "Макет · синхронизация", widthDp = 320, locale = "ru")
@Composable
private fun InactiveSensorStripPreview() = SinglePanelPreview(sensorPercent = 0, sensorActive = false)

@Preview(name = "01 · Что хотите синхронизировать?", group = "Макет · по нажатию", widthDp = 375, locale = "ru")
@Composable
private fun SyncChoiceStripPreview() = SinglePanelPreview(detail = DashboardPanelDetail.SYNC_CHOICE)

@Preview(name = "02 · Ресурс датчика · 52% · 4 дня", group = "Макет · по нажатию", widthDp = 375, locale = "ru")
@Composable
private fun SensorResourceStripPreview() = SinglePanelPreview(detail = DashboardPanelDetail.SENSOR)

@Preview(name = "03 · Глюкометр · подпись из макета", group = "Макет · по нажатию", widthDp = 375, locale = "ru")
@Composable
private fun MeterInfoStripPreview() = SinglePanelPreview(detail = DashboardPanelDetail.METER)

@Composable
private fun SinglePanelPreview(
    hasMeter: Boolean = true,
    sensorPercent: Int? = 52,
    sensorActive: Boolean = true,
    detail: DashboardPanelDetail? = null
) {
    MaterialTheme {
        ProvideTextStyle(TextStyle(fontFamily = GlucoseDashboardGothamPro)) {
            PanelSample("", hasMeter = hasMeter, sensorPercent = sensorPercent,
                sensorActive = sensorActive, detail = detail)
        }
    }
}
