package com.elta.android.presentation.features.consultant.viewmodel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.elta.android.domain.features.consultant.interactor.WebimChatStateUseCase
import com.elta.android.domain.features.consultant.interactor.WebimNetworkStateUseCase
import com.elta.android.domain.features.consultant.interactor.WebimSendMessageUseCase
import com.elta.android.domain.features.consultant.interactor.WebimSessionUseCase
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.Event
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.features.consultant.model.ConsultantViewState
import com.elta.android.presentation.features.consultant.model.toUi
import com.elta.android.presentation.features.consultant.widgets.ConsultantBottomAppBarWidgetModel
import com.elta.android.presentation.features.consultant.widgets.ConsultantTopAppBarWidgetModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

class ConsultantViewModel @Inject constructor(
    private val webimSession: WebimSessionUseCase,
    private val webimNetworkState: WebimNetworkStateUseCase,
    private val webimChatState: WebimChatStateUseCase,
    private val sendMessage: WebimSendMessageUseCase
) : BaseViewModel<ConsultantViewState, Event, ConsultantAction>(), LifecycleEventObserver {

    override fun createInitState(): ConsultantViewState =
        ConsultantViewState(
            webimConnectState = ConnectState.Connecting,
            chat = emptyList()
        )

    internal val consultantTopAppBar = ConsultantTopAppBarWidgetModel()
    internal val consultantBottomAppBar = ConsultantBottomAppBarWidgetModel()

    init {
        launch {
            webimNetworkState()
                .catch { handleError(it) }
                .collectLatest {
                    consultantTopAppBar.setConnectState(it.toUi())
                }
        }
    }

    override val widgets: List<BaseWidgetModel<*>> = listOf(
        consultantTopAppBar,
        consultantBottomAppBar
    ).actionObserve()

    override fun reduceStateByAction(
        currentState: ConsultantViewState,
        action: Action
    ): ConsultantViewState =
        when (action) {
            ConsultantAction.SearchClick -> currentState
            is ConsultantAction.SendMessageClick -> sendNewMessage(action.text)
            else -> {
                when (action) {
                    AppAction.BackPressure -> router.exit()
                }
                currentState
            }
        }

    private fun sendNewMessage(text: String): ConsultantViewState {
        launch {
            sendMessage(text)
        }
        consultantBottomAppBar.setText(null)
        return state.value.copy()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                webimSession.create()
            }

            Lifecycle.Event.ON_RESUME -> {
                webimSession.onResume()
            }

            Lifecycle.Event.ON_PAUSE -> webimSession.onPause()
            Lifecycle.Event.ON_DESTROY -> webimSession.onDestroy()
            else -> {}
        }
    }
}
