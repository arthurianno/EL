package com.elta.android.presentation.features.app.pm

import android.net.Uri
import com.elta.android.domain.features.auth.interactor.CheckEmailUseCase
import com.elta.android.domain.features.auth.interactor.IsUserLoggedInUseCase
import com.elta.android.domain.features.user.interactor.IsOnboardingPassedUseCase
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
import io.reactivex.Single
import javax.inject.Inject

class AppPm @Inject constructor(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val checkEmailUseCase: CheckEmailUseCase,
    private val isOnboardingPassedUseCase: IsOnboardingPassedUseCase,
    services: ServiceFacade
) : BasePm(services), ConnectionListener {

    val coldStartAction = Action<Unit>()
    val notificationStartAction = Action<Uri>()
    val deepLinkAction = Action<Uri>()
    val coldStartDeepLinkAction = Action<Uri>()
    val onStopAction = Action<String>()

    val syncProgress = Command<Boolean>(bufferSize = 1)

    @Suppress("LongMethod")
    override fun onCreate() {
        super.onCreate()

        coldStartAction.observable
            .skipWhileInProgress()
            .flatMapSingle {
                isUserLoggedInUseCase.execute()
                    .flatMap { isUserLoggedIn ->
                        when (isUserLoggedIn) {
                            true -> checkEmailUseCase.execute()
                                .flatMap(::checkEmailAndOnboarding)
                            else -> Single.just(Unit)
                                .doOnSuccess { router.newRootFlow(Screens.GreetingFlow) }
                        }
                    }
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
            .map { NotificationNavigationMapper.notificationDataToScreen(it) }
            .doOnNext { screen -> screen?.let { router.newRootFlow(it) } }
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
    }

    private fun checkEmailAndOnboarding(isEmailConfirmed: Boolean) =
        when (isEmailConfirmed) {
            true -> checkIsOnboardingPassed()
            else -> Single.just(Unit)
                .doOnSuccess { router.newRootChain(Screens.GreetingFlow, Screens.ActivateProfile) }
        }

    private fun checkIsOnboardingPassed(): Single<Unit> =
        isOnboardingPassedUseCase.execute()
            .doOnSuccess { isOnboardingPassed ->
                when (isOnboardingPassed) {
                    true -> router.newRootFlow(Screens.HomeFlow)
                    else -> router.newRootFlow(Screens.OnBoardingFlow)
                }
            }
            .map { Unit }
}