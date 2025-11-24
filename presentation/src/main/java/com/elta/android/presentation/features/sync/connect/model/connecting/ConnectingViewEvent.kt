package com.elta.android.presentation.features.sync.connect.model.connecting

import com.elta.android.presentation.core.compose.common.Event

sealed interface ConnectingViewEvent : Event {
    object Location {
        data object RequestPermission : ConnectingViewEvent
        data object Enable : ConnectingViewEvent
    }

    data object EnableBluetooth : ConnectingViewEvent
    data object ShowSheet : ConnectingViewEvent
    data object HideSheet : ConnectingViewEvent
    data object OpenSettings : ConnectingViewEvent
}
