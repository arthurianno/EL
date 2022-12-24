package com.elta.android.presentation.features.consultant.viewmodel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.elta.android.domain.features.consultant.interactor.WebimSessionUseCase
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.Event
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.features.consultant.model.ConsultantViewState
import com.elta.android.presentation.features.consultant.widgets.ConsultantBottomAppBarWidgetModel
import com.elta.android.presentation.features.consultant.widgets.ConsultantTopAppBarWidgetModel
import javax.inject.Inject

class ConsultantViewModel @Inject constructor(
    private val webimSession: WebimSessionUseCase
) : BaseViewModel<ConsultantViewState, Event, ConsultantAction>(), LifecycleEventObserver {
    override fun createInitState(): ConsultantViewState =
        ConsultantViewState(
            webimConnectState = ConnectState.Connect
        )

    internal val consultantTopAppBar = ConsultantTopAppBarWidgetModel()
    internal val consultantBottomAppBar = ConsultantBottomAppBarWidgetModel()

    override val widgets: List<BaseWidgetModel<*>> = listOf(
        consultantTopAppBar,
        consultantBottomAppBar
    ).actionObserve()

    override fun reduceStateByAction(
        currentState: ConsultantViewState,
        action: Action
    ): ConsultantViewState =
        when (action) {
            ConsultantAction.SearchClick -> currentState
            else -> {
                when (action) {
                    AppAction.BackPressure -> router.exit()
                }
                currentState
            }
        }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {}
            Lifecycle.Event.ON_START -> webimSession.onResume()
            Lifecycle.Event.ON_RESUME -> webimSession.onResume()
            Lifecycle.Event.ON_PAUSE -> webimSession.onPause()
            Lifecycle.Event.ON_STOP -> {}
            Lifecycle.Event.ON_DESTROY -> webimSession.onDestroy()
            Lifecycle.Event.ON_ANY -> {}
        }
    }
}
