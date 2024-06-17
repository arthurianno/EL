package com.elta.android.presentation.features.devices.search.viewmodel

import android.os.Bundle
import com.elta.android.domain.features.devices.interactor.LocateGlucometerUserCase
import com.elta.android.presentation.analytic.core.analytics.Analytics
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.features.devices.search.ADDRESS_ARGUMENT_ID
import com.elta.android.presentation.features.devices.search.model.GlucometerSearchAction
import com.elta.android.presentation.features.devices.search.model.GlucometerSearchStatus
import com.elta.android.presentation.features.devices.search.model.GlucometerSearchViewState
import com.elta.android.presentation.features.devices.search.model.SnackBarText
import com.elta.android.presentation.features.devices.search.widgets.GlucometerSearchButtonWidgetModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class GlucometerSearchViewModel @Inject constructor(
    private val findGlucometer: LocateGlucometerUserCase,
    private val analytics: Analytics
) : BaseViewModel<GlucometerSearchViewState>() {
    override fun createInitState(): GlucometerSearchViewState =
        GlucometerSearchViewState(
            searchStatus = GlucometerSearchStatus.Off,
            glucometerAddress = "",
            snackBar = SnackBarText.Connecting
        )

    private var findingJob: Job? = null
    internal val appBar = BaseAppTopBarWidgetModel()
    internal val searchButton = GlucometerSearchButtonWidgetModel()
    internal val cancelSearchDialog = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = { closeFragment() }
    )
    internal val cancelRingDialog = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = { closeFragment() }
    )

    override val widgets = listOf(
        appBar,
        searchButton
    ).actionObserve()

    override fun handleFragmentArguments(arguments: Bundle) {
        reduceState {
            state.value.copy(glucometerAddress = arguments.getString(ADDRESS_ARGUMENT_ID).orEmpty())
        }
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            is GlucometerSearchAction.StartConnection -> connectAndLaunchSearch()
            is AppAction.BackPressure -> backClick()
        }
    }

    override fun reduceStateByAction(
        currentState: GlucometerSearchViewState,
        action: Action,
    ): GlucometerSearchViewState =
        if (action is GlucometerSearchAction.StopSearch) {
            disableSearch(currentState)
        } else {
            currentState
        }

    override fun backClick() {
        when (state.value.searchStatus) {
            GlucometerSearchStatus.Connecting -> cancelSearchDialog.dialogOpen()
            GlucometerSearchStatus.On -> cancelRingDialog.dialogOpen()
            else -> closeFragment()
        }
    }

    private fun closeFragment() {
        findingJob?.cancel()
        router.exit()
    }

    private fun disableSearch(currentState: GlucometerSearchViewState): GlucometerSearchViewState {
        findingJob?.cancel()
        searchButton.resetSearch()
        return currentState.copy(searchStatus = GlucometerSearchStatus.Off)
    }

    private fun connectAndLaunchSearch() {
        findingJob?.cancel()
        findingJob = launch {
            analytics.trackEvent(AnalyticsEvent(name = AnalyticsEventType.FIND_GLUCOMETER))
            findGlucometer(state.value.glucometerAddress)
                .catch {
                    reduceState { state.value.copy(searchStatus = GlucometerSearchStatus.DeviceNotFound) }
                    searchButton.resetSearch()
                }
                .onStart {
                    reduceState {
                        state.value.copy(
                            searchStatus = GlucometerSearchStatus.Connecting,
                            snackBar = SnackBarText.Connecting
                        )
                    }
                }
                .collect {
                    if (state.value.searchStatus == GlucometerSearchStatus.Connecting) {
                        searchButton.deviceConnect()
                        reduceState { state.value.copy(searchStatus = GlucometerSearchStatus.On) }
                    }
                }
        }
    }
}
