package com.elta.android.presentation.features.auth.login.pm

import com.elta.android.domain.features.auth.interactor.LoginUseCase
import com.elta.android.domain.features.auth.interactor.LoginWithSocialNetworkUseCase
import com.elta.android.domain.features.user.interactor.GetUserIdUseCase
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.elta.android.domain.features.userinfo.interactor.GetUserInfoUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.Screens.ActivateProfile
import com.elta.android.presentation.analytics.model.AnalyticsEventParam
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.analytics.updateStableParam
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.registration.main.pm.BaseSocialPm
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class LoginPm @Inject constructor(
    private val loginWithSocialNetworkUseCase: LoginWithSocialNetworkUseCase,
    private val loginUseCase: LoginUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    services: ServiceFacade
) : BaseSocialPm(services) {

    @Suppress("LongMethod")
    override fun onCreate() {
        super.onCreate()

        Observables.combineLatest(
            isEmailValidState.observable,
            isPasswordValidState.observable
        )
            .map { it.first && it.second }
            .subscribe(continueEnabledState.consumer)
            .untilDestroy()

        menuAction.observable
            .trackEvent(AnalyticsEventType.PASSWORD_RECOVERY)
            .subscribe { router.navigateTo(Screens.PasswordRecovery) }
            .untilDestroy()

        continueAction.observable
            .skipWhileInProgress()
            .map(::createLoginParams)
            .flatMapSingle { params ->
                loginUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .updateAnalyticStableParam()
                    .trackEvent(AnalyticsEventType.LOG_IN)
                    .flatMap(::checkEmailAndOnBoarding)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        socialAction.observable
            .skipWhileInProgress()
            .map(::createLoginSocialParams)
            .flatMapSingle { params ->
                loginWithSocialNetworkUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .updateAnalyticStableParam()
                    .trackEvent(
                        AnalyticsEventType.LOG_IN,
                        AnalyticsEventParam.LOG_TYPE to params.network.name
                    )
                    .flatMap(::checkEmailAndOnBoarding)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun createLoginParams(i: Unit): LoginUseCase.Params =
        LoginUseCase.Params(emailInput.text.value, passwordInput.text.value)

    private fun createLoginSocialParams(network: SocialNetworkType): LoginWithSocialNetworkUseCase.Params =
        LoginWithSocialNetworkUseCase.Params(network)

    private fun Single<Boolean>.updateAnalyticStableParam(): Single<Boolean> =
        this.flatMap { isEmailActivated ->
            getUserIdUseCase.execute()
                .doOnSuccess { updateStableParam(id = it) }
                .map { isEmailActivated }
        }

    private fun checkEmailAndOnBoarding(i: Boolean) =
        getUserInfoUseCase.execute()
            .doOnSuccess { info ->
                when {
                    info.isEmailConfirmed?.not() ?: false -> router.navigateTo(ActivateProfile)
                    info.isOnBoardingPassed ?: false -> router.newRootFlow(Screens.HomeFlow)
                    else -> router.newRootFlow(Screens.OnBoardingFlow)
                }
            }
}