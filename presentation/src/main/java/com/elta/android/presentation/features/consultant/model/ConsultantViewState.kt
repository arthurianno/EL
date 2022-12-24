package com.elta.android.presentation.features.consultant.model

data class ConsultantViewState(
    val webimConnectState: ConnectState
)

enum class ConnectState {
    Connecting,
    Connect,
    Offline
}
