package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Bundle
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ConnectStartViewState
import javax.inject.Inject

class ConnectStartViewModel @Inject constructor() :
    BaseViewModel<ConnectStartViewState, ConnectAction>() {
    override fun createInitState() = ConnectStartViewState(
        isOnBoarding = true
    )

    internal val appTopBar = BaseAppTopBarWidgetModel()
    internal val downButton = DownButtonWidgetModel()

    override val widgets = listOf(
        appTopBar,
        downButton
    ).actionObserve()

    override fun handleFragmentArguments(arguments: Bundle) {
        reduceState {
            state.value.copy(
                isOnBoarding = arguments.getBoolean(
                    IS_ON_BOARDING_ARGUMENT_NAME
                )
            )
        }
    }

    override fun reduceStateByAction(
        currentState: ConnectStartViewState,
        action: Action
    ): ConnectStartViewState = run {
        when (action) {
            is DownButtonClick -> router.navigateTo(Screens.ConnectTypeScreen(state.value.isOnBoarding))
            is ConnectAction.SkipNextStep -> router.navigateTo(Screens.ShopsStart)
        }
        currentState
    }
}
