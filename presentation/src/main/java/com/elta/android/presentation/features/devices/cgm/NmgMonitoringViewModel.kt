package com.elta.android.presentation.features.devices.cgm

import com.elta.android.domain.features.cgm.model.NmgMonitoringState
import com.elta.android.domain.features.cgm.repository.NmgRepository
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import javax.inject.Inject

class NmgMonitoringViewModel @Inject constructor(
    private val nmgRepository: NmgRepository
) : BaseViewModel<NmgMonitoringState>() {
    internal val appBar = BaseAppTopBarWidgetModel()

    override val widgets = listOf(appBar).actionObserve()

    override fun createInitState() = NmgMonitoringState(null, null, 0)

    init {
        launch {
            nmgRepository.monitoringState().collect { monitoringState ->
                reduceState { monitoringState }
            }
        }
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            is AppAction.BackPressure -> backClick()
            NmgMonitoringAction.StopMonitoring -> {
                nmgRepository.stopMonitoring()
                router.exit()
            }
        }
    }
}
