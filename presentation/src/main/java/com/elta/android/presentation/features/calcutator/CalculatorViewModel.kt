package com.elta.android.presentation.features.calcutator

import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.Event
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.BaseAppTopBarWidgetModel
import com.elta.android.presentation.features.calcutator.model.CalculatorAction
import com.elta.android.presentation.features.calcutator.model.CalculatorState
import javax.inject.Inject

class CalculatorViewModel @Inject constructor() :
    BaseViewModel<CalculatorState, Event, CalculatorAction>(CalculatorState()) {

    val appTopBarWidgetModel = BaseAppTopBarWidgetModel()
    override val widgets: List<BaseWidgetModel<*>> = listOf(
        appTopBarWidgetModel
    ).actionObserve()

    override fun reduceStateByAction(
        currentState: CalculatorState,
        action: Action
    ): CalculatorState =
        when (action) {
            AppAction.BackPressure -> {
                router.exit()
                currentState
            }

            else -> currentState
        }
}
