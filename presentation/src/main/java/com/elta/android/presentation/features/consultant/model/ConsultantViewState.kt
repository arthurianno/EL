package com.elta.android.presentation.features.consultant.model

data class ConsultantViewState(
    val webimConnectState: ConnectState,
    val chat: List<ChatUiEntity>,
    val hasNewMessages: Boolean
)
