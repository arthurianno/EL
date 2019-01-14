package com.nullgr.android.presentation.core.pm.widgets

import com.nullgr.android.presentation.core.pm.BasePm
import com.nullgr.android.presentation.core.pm.ReactiveNetworkFacade
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

@Suppress("UnnecessaryParentheses", "UseDataClass")
class NetworkControl(
    network: ReactiveNetworkFacade,
    pm: BasePm
) {
    val observable: Observable<Boolean> = network.observeNetworkConnectivity()
        .map { it.available() }
        .publish { u ->
            Observable.merge(u.take(1).filter { !it }, u.skip(1))
        }
        .distinctUntilChanged()
        .throttleFirst(1, TimeUnit.SECONDS)
        .doOnNext { connected ->
            pm.networkStateAction.consumer.accept(connected)
        }
}

fun BasePm.networkControl(network: ReactiveNetworkFacade) =
    NetworkControl(network, this)