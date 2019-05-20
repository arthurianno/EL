package com.elta.android.presentation.features.app.pm

import android.net.Uri
import com.elta.android.domain.features.auth.interactor.CheckEmailUseCase
import com.elta.android.domain.features.auth.interactor.IsUserLoggedInUseCase
import com.elta.android.domain.features.user.interactor.IsOnboardingPassedUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.listeners.ConnectionListener
import com.elta.android.presentation.utils.dynamic_links.DynamicLinkNavigationMapper
import com.elta.android.presentation.utils.dynamic_links.NotificationNavigationMapper
import io.reactivex.Single
import timber.log.Timber
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

    override fun onCreate() {
        super.onCreate()

        coldStartAction.observable
            .skipWhileInProgress()
            .flatMapSingle {
                isUserLoggedInUseCase.execute()
                    .flatMap { isUserLoggedIn ->
                        if (isUserLoggedIn) {
//                            Timber.e("AppPm >> ${networkStateAction.observable.blockingLast()}")
//                            if (networkStateAction.observable.blockingLast()) {
//
//                            }
                            checkEmailUseCase.execute()
                                .doOnSuccess { isEmailConfirmed ->
                                    if (isEmailConfirmed) {
                                        router.newRootScreen(Screens.HomeFlow)
                                    } else {
                                        router.newRootChain(Screens.GreetingFlow, Screens.ActivateProfile)
                                    }
                                }
                                .doOnError {
                                    router.newRootScreen(Screens.HomeFlow)
                                }
                                .map { Unit }
                        } else {
                            Single.just(Unit)
                                .doOnSuccess { router.newRootScreen(Screens.GreetingFlow) }
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
    }
}