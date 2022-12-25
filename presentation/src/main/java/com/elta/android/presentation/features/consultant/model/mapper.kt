package com.elta.android.presentation.features.consultant.model // ktlint-disable filename

import com.elta.android.domain.features.consultant.model.WebimChatState
import com.elta.android.domain.features.consultant.model.WebimStatus

internal fun WebimStatus.toUi(): ConnectState =
    when (this) {
        WebimStatus.Online -> ConnectState.Connect
        WebimStatus.Offline -> ConnectState.Offline
        WebimStatus.Connecting -> ConnectState.Connecting
    }

internal fun WebimChatState.toUi(): ConnectState =
    when (this) {
        WebimChatState.Open -> ConnectState.Connect
        WebimChatState.Close -> ConnectState.Offline
        WebimChatState.Chatting -> ConnectState.Connecting
    }
