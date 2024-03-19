package com.elta.android.presentation.features.version.mandatory.viewmodel

import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.ClearEvent
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.features.version.mandatory.model.MandatoryUpdateEvent
import com.elta.android.presentation.features.version.mandatory.model.MandatoryUpdateViewState
import javax.inject.Inject

class MandatoryUpdateViewModel @Inject constructor() :
    BaseViewModel<MandatoryUpdateViewState>() {
    override fun createInitState(): MandatoryUpdateViewState = MandatoryUpdateViewState

    val downButton = DownButtonWidgetModel()

    override val widgets: List<BaseWidgetModel<*>> = listOf<BaseWidgetModel<*>>(
        downButton
    ).actionObserve()

    override fun handleUserAction(action: Action) {
        when (action) {
            DownButtonClick -> {
                sendEvent(ClearEvent)
                sendEvent(MandatoryUpdateEvent.OpenAppPageInStore)
            }
            else -> super.handleUserAction(action)
        }
    }

}
