package com.elta.android.presentation.features.main.records.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.math.min

/** Static design fixtures only. This activity has no bus, repository, or device connection. */
class DashboardPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scenario = intent.getStringExtra("scenario") ?: "meter"
        val width = intent.getIntExtra("widthDp", 360).coerceIn(240, 840)
        val height = intent.getIntExtra("heightDp", 640).coerceIn(240, 1200)
        val fontScale = intent.getFloatExtra("fontScale", 1f).coerceIn(1f, 2f)
        setContentView(ComposeView(this).apply {
            setContent {
                if (scenario == "connection_help") {
                    com.elta.android.presentation.theme.EltaTheme {
                        Box(Modifier.fillMaxSize().navigationBarsPadding()) {
                            com.elta.android.presentation.features.sync.connect.DmcConnectionIntroPreviewContent()
                        }
                    }
                    return@setContent
                }
                MaterialTheme {
                    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().background(Color.DarkGray)) {
                        val originalDensity = LocalDensity.current.density
                        val fittingScale = min(maxWidth.value / width, maxHeight.value / height).coerceAtMost(1f)
                        CompositionLocalProvider(LocalDensity provides Density(originalDensity * fittingScale, fontScale)) {
                            Box(Modifier.size(width.dp, height.dp).align(Alignment.TopCenter)) {
                                val meter = DashboardDevice("preview", "Сателлит", "1234567890", System.currentTimeMillis() - 18_000_000)
                                val sensor = DashboardSensor("preview", when (scenario) {
                                    "sensor_full" -> 100
                                    "sensor_low" -> 8
                                    "sensor_expired", "both_expired" -> 0
                                    else -> 52
                                }, 4, scenario !in listOf("sensor_expired", "both_expired"), System.currentTimeMillis() - 3_600_000)
                                val state = GlucoseDashboardUiState(
                                    device = meter.takeIf { scenario in listOf("meter", "both", "both_expired", "error", "info") },
                                    sensor = sensor.takeIf { scenario.startsWith("sensor") || scenario in listOf("both", "both_expired", "error", "info") },
                                    glucoseValue = "4,1",
                                    tirPercentage = "73%",
                                    breadUnitsText = "0,1 ХЕ",
                                    insulinText = "0,9 Ед.",
                                    glucoseState = when (intent.getStringExtra("glucose")) {
                                        "high" -> GlucoseState.HIGH
                                        "low" -> GlucoseState.LOW
                                        else -> GlucoseState.NORMAL
                                    },
                                    chartPoints = listOf(GlucosePoint("09:00", 4.1f), GlucosePoint("10:00", 5.5f))
                                )
                                var sync by remember {
                                    mutableStateOf(DashboardSyncUiState(
                                        displayedTime = "",
                                        statusMessage = when (scenario) {
                                            "error" -> "Не удалось синхронизировать устройство. Проверьте подключение и повторите попытку."
                                            "info" -> "Устройство синхронизировано"
                                            else -> null
                                        },
                                        isError = scenario == "error"
                                    ))
                                }
                                GlucoseDashboardContent(
                                    uiState = if (scenario == "empty") state.copy(glucoseValue = "—", chartPoints = emptyList()) else state,
                                    syncState = sync,
                                    onAction = { action ->
                                        if (action == GlucoseDashboardAction.DismissSyncMessage) sync = sync.copy(statusMessage = null)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        })
    }
}
