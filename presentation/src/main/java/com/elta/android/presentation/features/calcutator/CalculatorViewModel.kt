package com.elta.android.presentation.features.calcutator

import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.presentation.core.compose.Action
import com.elta.android.presentation.core.compose.BaseViewModel
import com.elta.android.presentation.core.compose.Event
import com.elta.android.presentation.features.calcutator.model.CalculatorAction
import com.elta.android.presentation.features.calcutator.model.CalculatorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.rx2.asFlow
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val getProfile: GetProfileUseCase
) : BaseViewModel<CalculatorState, Event, CalculatorAction>(CalculatorState()) {

    init {
        launch {
            getProfile.execute()
                .toObservable()
                .asFlow()
                .collect {
                    reduceState { state.value.copy(profile = it) }
                }
        }
    }

    override fun reduceStateByAction(
        currentState: CalculatorState,
        action: Action
    ): CalculatorState {
        TODO("Not yet implemented")
    }
}
