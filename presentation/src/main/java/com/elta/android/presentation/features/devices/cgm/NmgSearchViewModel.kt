package com.elta.android.presentation.features.devices.cgm

import com.elta.android.domain.features.cgm.model.NmgSensor
import com.elta.android.domain.features.cgm.repository.NmgRepository
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

data class NmgSearchViewState(
    val isScanning: Boolean = false,
    val sensors: List<NmgSensor> = emptyList(),
    val hasError: Boolean = false,
    val notificationsUnavailable: Boolean = false
)

class NmgSearchViewModel @Inject constructor(
    private val nmgRepository: NmgRepository
) : BaseViewModel<NmgSearchViewState>() {
    private var scanJob: Job? = null
    internal val appBar = BaseAppTopBarWidgetModel()

    override fun createInitState() = NmgSearchViewState()

    override val widgets = listOf(appBar).actionObserve()

    override fun handleUserAction(action: Action) {
        when (action) {
            is AppAction.BackPressure -> backClick()
            NmgSearchAction.StartScan -> startScan()
            is NmgSearchAction.NotificationPermissionResult -> reduceState {
                state.value.copy(notificationsUnavailable = !action.isGranted)
            }
            is NmgSearchAction.SelectSensor -> {
                nmgRepository.startMonitoring(action.sensorId)
                router.newRootScreen(Screens.HomeFlow)
            }
        }
    }

    private fun startScan() {
        scanJob?.cancel()
        scanJob = launch {
            nmgRepository.findSensors()
                .catch {
                    reduceState { state.value.copy(isScanning = false, hasError = true) }
                }
                .collect { sensors ->
                    reduceState {
                        state.value.copy(isScanning = true, sensors = sensors, hasError = false)
                    }
                }
        }
    }

    override fun onCleared() {
        scanJob?.cancel()
        super.onCleared()
    }
}
