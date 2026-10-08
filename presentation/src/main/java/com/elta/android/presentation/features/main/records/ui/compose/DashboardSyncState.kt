package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.elta.android.presentation.Events
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class DashboardSyncState(
    initialTime: String
) {
    var displayedTime by mutableStateOf(initialTime)
        private set
    var statusMessage by mutableStateOf<String?>(null)
        private set
    var isSyncing by mutableStateOf(false)
        private set
    var isError by mutableStateOf(false)
        private set
    private var retryTarget = DashboardSyncTarget.METER
    private var hideJob: Job? = null

    fun updateDisplayedTime(value: String) {
        if (!isSyncing) displayedTime = value
    }

    fun handle(event: Events.Sync, scope: CoroutineScope) {
        if (event is Events.Sync.Server) return
        when (event) {
            is Events.Sync.Glucometer.Started -> start(
                scope,
                "Подключение к устройству...",
                "Ошибка синхронизации с устройством"
            )
            is Events.Sync.Glucometer.Success,
            is Events.Sync.Glucometer.NoNewEvents,
            is Events.Sync.Glucometer.InvalidTime -> completed(scope, "Устройство синхронизировано")
            is Events.Sync.Glucometer.Error,
            is Events.Sync.Glucometer.ErrorWithMessage,
            is Events.Sync.Glucometer.Nothing -> showMessage(
                scope,
                "Ошибка синхронизации с устройством",
                4_000L, isError = true
            )
            is Events.Sync.Server -> Unit
        }
    }

    fun dismiss() {
        if (isSyncing) return
        hideJob?.cancel()
        statusMessage = null
        isError = false
    }

    fun showMessage(
        scope: CoroutineScope,
        message: String,
        timeoutMillis: Long,
        isError: Boolean = false
    ) {
        hideJob?.cancel()
        isSyncing = false
        statusMessage = message
        this.isError = isError
        if (isError) return
        hideJob = scope.launch {
            delay(timeoutMillis)
            statusMessage = null
        }
    }

    fun asUiState(): DashboardSyncUiState = DashboardSyncUiState(
        displayedTime = displayedTime,
        statusMessage = statusMessage,
        isSyncing = isSyncing,
        isError = isError,
        retryTarget = retryTarget
    )

    private fun start(
        scope: CoroutineScope,
        message: String,
        fallbackMessage: String
    ) {
        hideJob?.cancel()
        isSyncing = true
        isError = false
        statusMessage = message
        hideJob = scope.launch {
            delay(SyncFallbackTimeoutMillis)
            if (isSyncing) showMessage(scope, fallbackMessage, 3_000L, isError = true)
        }
    }

    private fun completed(scope: CoroutineScope, message: String) {
        showMessage(scope, message, 3_000L)
    }
}

@Composable
internal fun rememberDashboardSyncState(initialTime: String): DashboardSyncState =
    remember {
        DashboardSyncState(
            initialTime = initialTime
        )
    }

private const val SyncFallbackTimeoutMillis = 60_000L
