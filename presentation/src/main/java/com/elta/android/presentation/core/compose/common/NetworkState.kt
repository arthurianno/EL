package com.elta.android.presentation.core.compose.common

sealed class NetworkState {
    object Available : NetworkState()
    object Unavailable : NetworkState()
}
