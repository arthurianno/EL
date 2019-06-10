package com.elta.android.presentation.features.app.pm

import android.net.Uri
import com.elta.android.domain.features.userinfo.interactor.GetUserInfoUseCase
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytics.model.AnalyticsEvent
import com.elta.android.presentation.analytics.model.AnalyticsEventParam
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.listeners.ConnectionListener
import com.elta.android.presentation.utils.dynamic_links.DynamicLinkNavigationMapper
import com.elta.android.presentation.utils.dynamic_links.NotificationNavigationMapper
import javax.inject.Inject

class AppPm @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    services: ServiceFacade
) : BasePm(services), ConnectionListener {

    val coldStartAction = Action<Unit>()
    val notificationStartAction = Action<Uri>()
    val deepLinkAction = Action<Uri>()
    val coldStartDeepLinkAction = Action<Uri>()
    val onStopAction = Action<String>()

    val syncProgress = Command<Boolean>(bufferSize = 1)
    val backendSyncProgress = Command<Boolean>(bufferSize = 1)

    @Suppress("LongMethod")
    override fun onCreate() {
        super.onCreate()

        coldStartAction.observable
            .skipWhileInProgress()
            .flatMapSingle {
                getUserInfoUseCase.execute()
                    .doOnSuccess { user ->
                        when {
                            !(user.isUserLoggedIn ?: false) -> router.newRootFlow(Screens.GreetingFlow)
                            !(user.isEmailConfirmed ?: false) -> router.newRootChain(
                                Screens.GreetingFlow, Screens.ActivateProfile
                            )
                            !(user.isOnBoardingPassed ?: false) -> router.newRootFlow(Screens.OnBoardingFlow)
                            else -> router.newRootFlow(Screens.HomeFlow)
                        }
                    }
                    .doOnError { router.newRootFlow(Screens.GreetingFlow) }
                    .bindProgress()
            }
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
            .flatMapSingle { uri ->
                getUserInfoUseCase.execute()
                    .map { Pair(uri, it) }
                    .doOnSuccess(::handleNotification)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        onStopAction.observable
            .trackEvent {
                AnalyticsEvent(
                    AnalyticsEventType.APP_EXIT,
                    hashMapOf(AnalyticsEventParam.SCREEN_NAME to it)
                )
            }
            .subscribe()
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }.map { Unit }
            .trackEvent(AnalyticsEventType.APP_LAUNCH)
            .subscribe()
            .untilDestroy()

        bus.events<Events.SyncProgress>()
            .skip(1)
            .map(Events.SyncProgress::inProgress)
            .map { !it }
            .subscribe(syncProgress.consumer)
            .untilDestroy()

        bus.events<Events.BackendSyncProgress>()
            .skip(1)
            .map(Events.BackendSyncProgress::inProgress)
            .map { !it }
            .subscribe(backendSyncProgress.consumer)
            .untilDestroy()
    }

    private fun handleNotification(pair: Pair<Uri, UserInfo>) {
        if (pair.second.isUserLoggedIn == true)
            NotificationNavigationMapper.notificationDataToScreen(pair.first)?.let { it ->
                router.newRootScreen(it)
            }
        else router.newRootChain(Screens.GreetingFlow, Screens.AuthFlow)
    }

    override fun handleError(error: Throwable) {
        if (error is NoSuchElementException) router.newRootChain(Screens.GreetingFlow, Screens.AuthFlow)
        else super.handleError(error)
    }
}