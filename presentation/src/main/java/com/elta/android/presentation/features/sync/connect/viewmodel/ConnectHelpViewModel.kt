package com.elta.android.presentation.features.sync.connect.viewmodel

import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ConnectHelpViewState
import javax.inject.Inject

class ConnectHelpViewModel @Inject constructor() :
    BaseViewModel<ConnectHelpViewState, ConnectAction>() {
    override fun createInitState(): ConnectHelpViewState =
        ConnectHelpViewState(
            id = ""
        )

    internal val appTopBar = BaseAppTopBarWidgetModel()

    override val widgets = listOf(
        appTopBar
    ).actionObserve()

    override fun reduceStateByAction(
        currentState: ConnectHelpViewState,
        action: Action
    ): ConnectHelpViewState = run {
        if (action is AppAction.BackPressure) backClick()
        currentState
    }
}
