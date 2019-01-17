package com.elta.android.presentation.core.pm

import android.content.Context
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.nullgr.core.hardware.NetworkChecker
import io.reactivex.Observable
import javax.inject.Inject

class ReactiveNetworkFacade @Inject constructor(
    private val context: Context,
    private val networkChecker: NetworkChecker
) {

    fun observeNetworkConnectivity(): Observable<Connectivity> = ReactiveNetwork.observeNetworkConnectivity(context)

    fun isConnectedOverWifi(): Boolean = networkChecker.isConnectedOverWifi()
}