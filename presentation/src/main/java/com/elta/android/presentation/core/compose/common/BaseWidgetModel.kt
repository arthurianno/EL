package com.elta.android.presentation.core.compose.common

import androidx.compose.runtime.Stable
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
abstract class BaseWidgetModel<D> {
    private val initState: D
        get() = createInitState()

    private var viewModelActionReceiver: ((action: Action) -> Unit)? = null
    fun registrationActionReceiver(receiver: (Action) -> Unit) {
        viewModelActionReceiver = receiver
    }

    private var viewModel: BaseViewModel<*>? = null

    protected abstract fun createInitState(): D

    private val _state = MutableStateFlow(initState)

    val state: StateFlow<D> = _state.asStateFlow()
    private val _action = MutableSharedFlow<Action>(extraBufferCapacity = 1)

    val action: SharedFlow<Action>
        get() = _action.asSharedFlow()

    infix fun sendAction(action: Action) {
        viewModelActionReceiver?.let { it(action) } ?: _action.tryEmit(action)
    }

    protected fun setState(state: () -> D) {
        _state.value = state()
    }
}
