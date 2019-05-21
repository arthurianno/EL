package com.elta.android.presentation.core.pm

import com.elta.android.presentation.analytics.core.Analytics
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UseDataClass")
@Singleton
class ServiceFacade @Inject constructor(
    resourceProvider: ResourceProvider,
    networkFacade: ReactiveNetworkFacade,
    appAnalytics: Analytics,
    rxBus: RxBus,
    apiErrorParser: ExceptionParser
) {
    val resources: ResourceProvider = resourceProvider
    val network: ReactiveNetworkFacade = networkFacade
    val analytics: Analytics = appAnalytics
    val bus: RxBus = rxBus
    val errorParser: ExceptionParser = apiErrorParser
}