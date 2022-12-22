package com.elta.android.presentation.features.consultant.viewmodel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.elta.android.domain.features.consultant.interactor.WebimSessionUseCase
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.Event
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.features.consultant.model.MainChartAction
import com.elta.android.presentation.features.consultant.model.MainChartViewState
import javax.inject.Inject

class MainChartViewModel @Inject constructor(
    private val webimSession: WebimSessionUseCase
) : BaseViewModel<MainChartViewState, Event, MainChartAction>(), LifecycleEventObserver {
    override fun createInitState(): MainChartViewState =
        MainChartViewState(
            id = ""
        )

    override fun reduceStateByAction(
        currentState: MainChartViewState,
        action: Action
    ): MainChartViewState =
        currentState

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
