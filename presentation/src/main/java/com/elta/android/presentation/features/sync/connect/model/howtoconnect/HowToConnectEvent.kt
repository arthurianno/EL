package com.elta.android.presentation.features.sync.connect.model.howtoconnect

import com.elta.android.presentation.core.compose.common.Event

sealed interface HowToConnectEvent : Event {

    object Location {
        data object RequestPermission : HowToConnectEvent
        data object Enable : HowToConnectEvent
    }

    object Bluetooth {
        data object RequestPermission : HowToConnectEvent
        data object Enable : HowToConnectEvent
    }

    data object OpenSettings : HowToConnectEvent
    data object RequestCameraPermission : HowToConnectEvent
}
