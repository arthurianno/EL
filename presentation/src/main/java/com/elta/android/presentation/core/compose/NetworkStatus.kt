package com.elta.android.presentation.core.compose

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.elta.android.presentation.core.compose.common.NetworkState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun Context.networkStateAsFlow() = callbackFlow {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val callback = networkCallback { connectionState -> trySend(connectionState) }
    val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
    connectivityManager.registerNetworkCallback(networkRequest, callback)
    val currentState = getCurrentConnectivityState(connectivityManager)
    trySend(currentState)
    awaitClose {
        connectivityManager.unregisterNetworkCallback(callback)
    }
}

private fun networkCallback(callback: (NetworkState) -> Unit): ConnectivityManager.NetworkCallback {
    return object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            callback(NetworkState.Available)
        }

        override fun onLost(network: Network) {
            callback(NetworkState.Unavailable)
        }
    }
}

private fun getCurrentConnectivityState(
    connectivityManager: ConnectivityManager
): NetworkState = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    ?.let { actNetwork ->
        when {
            actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkState.Available

            else -> NetworkState.Unavailable
        }
    } ?: NetworkState.Unavailable
