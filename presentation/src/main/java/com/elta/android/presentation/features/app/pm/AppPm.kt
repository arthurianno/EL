package com.elta.android.presentation.features.app.pm

import android.net.Uri
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.listeners.ConnectionListener
import com.elta.android.presentation.utils.dynamic_links.DynamicLinkNavigationMapper
import com.elta.android.presentation.utils.dynamic_links.NotificationNavigationMapper
import javax.inject.Inject

class AppPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services), ConnectionListener {

    val coldStartAction = Action<Unit>()
    val notificationStartAction = Action<Uri>()
    val deepLinkAction = Action<Uri>()
    val coldStartDeepLinkAction = Action<Uri>()

    val syncProgress = Command<Boolean>(bufferSize = 1)

    override fun onCreate() {
        super.onCreate()

        coldStartAction.observable
            .doOnNext { router.newRootScreen(Screens.HomeFlow) }
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

        notificationStartAction.observable
            .map { NotificationNavigationMapper.notificationDataToScreen(it) }
            .doOnNext { screen -> screen?.let { router.newRootFlow(it) } }
            .retry()
            .subscribe()
            .untilDestroy()

        bus.events<Events.SyncProgress>()
            .skip(1)
            .map(Events.SyncProgress::inProgress)
            .map { !it }
            .subscribe(syncProgress.consumer)
            .untilDestroy()
    }
}