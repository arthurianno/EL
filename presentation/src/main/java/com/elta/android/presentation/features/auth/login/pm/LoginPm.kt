package com.elta.android.presentation.features.auth.login.pm

import com.elta.android.common.logger.FirebaseStorage
import com.elta.android.domain.features.auth.interactor.LoginUseCase
import com.elta.android.domain.features.user.interactor.GetUserIdUseCase
import com.elta.android.domain.features.userinfo.interactor.GetProfileSettingsUseCase
import com.elta.android.domain.features.userinfo.interactor.GetUserInfoUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.Screens.ActivateProfile
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.analytics.updateStableParam
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.reminders.utils.RemindersManager
import com.elta.android.presentation.features.registration.main.pm.BaseRegistrationPm
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class LoginPm @Inject constructor(
    private val remindersManager: RemindersManager,
    private val loginUseCase: LoginUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getProfileSettings: GetProfileSettingsUseCase,
    private val getUserId: GetUserIdUseCase,
    private val firebaseStorage: FirebaseStorage,
    services: ServiceFacade
) : BaseRegistrationPm(services) {

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
    }

    private fun createLoginParams(i: Unit): LoginUseCase.Params =
        LoginUseCase.Params(emailInput.text.value, passwordInput.text.value)

    private fun Single<Boolean>.updateAnalyticStableParam(): Single<Boolean> =
        this.flatMap { isEmailActivated ->
            if (isEmailActivated) {
                getUserIdUseCase.execute()
                    .doOnSuccess { updateStableParam(id = it) }
                    .map { isEmailActivated }
            } else {
                Single.just(isEmailActivated)
            }
        }

    private fun checkEmailAndOnBoarding(i: Boolean) =
        getUserInfoUseCase.execute()
            .flatMap { userInfo ->
                getProfileSettings.execute()
                    .map { userInfo to it.isOnboarded }
            }
            .doOnSuccess { info ->
                getUserId.execute()
                    .doOnSuccess { firebaseStorage.userLogin = it }
                    .subscribe()
                when {
                    !info.first.isEmailConfirmed -> router.navigateTo(ActivateProfile)
                    info.second -> {
                        remindersManager.scheduleReminders()
                        router.newRootFlow(Screens.HomeFlow)
                    }

                    else -> router.newRootFlow(Screens.OnBoardingFlow)
                }
            }
}
