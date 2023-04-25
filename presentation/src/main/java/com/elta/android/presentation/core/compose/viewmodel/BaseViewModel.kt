package com.elta.android.presentation.core.compose.viewmodel

import android.os.Bundle
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.Event
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@Stable
abstract class BaseViewModel<ST> : ViewModel() {
    private val initState: ST
        get() = createInitState()

    protected abstract fun createInitState(): ST

    private var _router: Router? = null
    val router: Router
        get() = checkNotNull(_router)

    protected open val widgets: List<BaseWidgetModel<*>> = emptyList()

    private val _state = MutableStateFlow(initState)
    val state: StateFlow<ST>
        get() = _state.asStateFlow()
    private val action = MutableSharedFlow<Action>()

    private val _event = MutableSharedFlow<Event?>()

    val event: SharedFlow<Event?>
        get() = _event.asSharedFlow()

    init {
        this.action
            .onEach {
                handleUserAction(it)
                _state.tryEmit(reduceStateByAction(state.value, it))
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, initState)
    }

    fun setRouter(router: Router) {
        _router = router
    }

    internal fun routerIsNotSet(): Boolean = _router == null

    open fun handleFragmentArguments(arguments: Bundle) {}

    open fun backClick() {
        router.exit()
    }

    val actionReceiver: (Action) -> Unit = { action ->
        sendAction(action)
    }

    infix fun sendAction(action: Action) {
        launch {
            this@BaseViewModel.action.emit(action)
        }
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launch {
            block(this)
        }

    protected open fun handleError(error: Throwable, message: String? = null) {
        Timber.e(error, message ?: error.message.orEmpty())
    }

    protected fun sendEvent(event: Event) {
        launch {
            _event.emit(event)
        }
    }

    protected fun reduceState(reduceBlock: () -> ST) {
        _state.tryEmit(reduceBlock())
    }

    protected open fun reduceStateByAction(currentState: ST, action: Action): ST = currentState
    protected open fun handleUserAction(action: Action) {}

    protected fun List<BaseWidgetModel<*>>.actionObserve() = this.also { widgets ->
        widgets.map { it.action }
            .merge()
            .onEach { action ->
                this@BaseViewModel sendAction action
            }
            .shareIn(viewModelScope, SharingStarted.Eagerly)
    }
}
