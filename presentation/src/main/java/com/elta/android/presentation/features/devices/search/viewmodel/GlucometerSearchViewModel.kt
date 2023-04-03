package com.elta.android.presentation.features.devices.search.viewmodel

import com.elta.android.domain.features.devices.usecase.FindGlucometerUseCase
import com.elta.android.presentation.analytics.core.Analytics
import com.elta.android.presentation.analytics.model.AnalyticsEvent
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
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
    private val findGlucometer: FindGlucometerUseCase,
    private val analytics: Analytics
) : BaseViewModel<GlucometerSearchViewState, GlucometerSearchAction>() {
    override fun createInitState(): GlucometerSearchViewState =
        GlucometerSearchViewState(
            searchStatus = GlucometerSearchStatus.Off,
            snackBar = SnackBarText.Connecting
        )

    private var glucometerAddress: String = ""
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

    fun setGlucometerAddress(address: String) {
        glucometerAddress = address
    }

    override fun reduceStateByAction(
        currentState: GlucometerSearchViewState,
        action: Action,
    ): GlucometerSearchViewState =
        if (action is GlucometerSearchAction.StopSearch) {
            disableSearch(currentState)
        } else {
            when (action) {
                is GlucometerSearchAction.StartConnection -> connectAndLaunchSearch()
                is AppAction.BackPressure -> backClick()
            }
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
            findGlucometer(glucometerAddress)
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
