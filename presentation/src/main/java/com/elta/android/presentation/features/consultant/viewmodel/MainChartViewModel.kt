package com.elta.android.presentation.features.consultant.viewmodel

import com.elta.android.domain.features.consultant.interactor.WebimSessionUseCase
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.Event
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.features.consultant.model.MainChartAction
import com.elta.android.presentation.features.consultant.model.MainChartViewState
import javax.inject.Inject

class MainChartViewModel @Inject constructor(
    val webimSession: WebimSessionUseCase
) : BaseViewModel<MainChartViewState, Event, MainChartAction>() {
    override fun createInitState(): MainChartViewState =
        MainChartViewState(
            id = ""
        )

    override fun reduceStateByAction(
        currentState: MainChartViewState,
        action: Action
    ): MainChartViewState =
        currentState
}
