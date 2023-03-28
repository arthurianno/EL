package com.elta.android.presentation.features.sync.connect.model

import com.elta.android.presentation.core.compose.common.Event

sealed class ConnectMainEvent : Event {
    override fun equals(other: Any?) = false
    override fun hashCode() = System.identityHashCode(this)

    class ShowSheet : ConnectMainEvent()
    class HideSheet : ConnectMainEvent()
}
