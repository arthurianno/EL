package com.elta.android.presentation.features.calcutator

import com.elta.android.presentation.core.viewmodel.Action
import com.elta.android.presentation.core.viewmodel.BaseViewModel
import com.elta.android.presentation.core.viewmodel.Event
import com.elta.android.presentation.features.calcutator.model.CalculatorAction
import com.elta.android.presentation.features.calcutator.model.CalculatorState
import javax.inject.Inject

class CalculatorViewModel @Inject constructor() :
    BaseViewModel<CalculatorState, Event, CalculatorAction>(CalculatorState()) {

    override fun reduceStateByAction(
        currentState: CalculatorState,
        action: Action
    ): CalculatorState {
        TODO("Not yet implemented")
    }
}
