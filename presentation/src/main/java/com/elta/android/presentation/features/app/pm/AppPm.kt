package com.elta.android.presentation.features.app.pm

import android.net.Uri
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.listeners.ConnectionListener
import com.elta.android.presentation.utils.dynamic_links.DynamicLinkNavigationMapper
import javax.inject.Inject

class AppPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services), ConnectionListener {

    val coldStartAction = Action<Unit>()
    val deepLinkAction = Action<Uri>()
    val coldStartDeepLinkAction = Action<Uri>()

    override fun onCreate() {
        super.onCreate()

        coldStartAction.observable
            .doOnNext { router.newRootScreen(Screens.OnBoardingFlow) }
            .retry()
            .subscribe()
            .untilDestroy()

        deepLinkAction.observable
            .map { DynamicLinkNavigationMapper.deepLinkToScreen(it) }
            .doOnNext { router.navigateTo(it) }
            .subscribe()
            .untilDestroy()

        coldStartDeepLinkAction.observable
            .map { DynamicLinkNavigationMapper.deepLinkToScreen(it) }
            .doOnNext { router.newRootChain(Screens.GreetingFlow, it) }
            .subscribe()
            .untilDestroy()
    }
}