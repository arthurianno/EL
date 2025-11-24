package com.elta.android.presentation.features.auth.login.pm

import com.elta.android.common.logger.FirebaseStorage
import com.elta.android.domain.features.auth.interactor.LoginUseCase
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.interactor.GetUserIdUseCase
import com.elta.android.domain.features.userinfo.interactor.GetProfileSettingsUseCase
import com.elta.android.domain.features.userinfo.interactor.GetUserInfoUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.Screens.ActivateProfile
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.getMetricAttributes
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.updateStableParam
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.reminders.utils.RemindersManager
import com.elta.android.presentation.features.registration.main.variantA.pm.BaseRegistrationPmVariantA
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

// fixme Variant A : recovery_account
class LoginPmVariantA @Inject constructor(
    private val remindersManager: RemindersManager,
    private val loginUseCase: LoginUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getProfileSettings: GetProfileSettingsUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val getUserId: GetUserIdUseCase,
    private val getFeatureConfigUseCase: GetFeatureConfigUseCase,
    private val firebaseStorage: FirebaseStorage,
    private val appMetric: AppMetricTracker,
    services: ServiceFacade
) : BaseRegistrationPmVariantA(services) {

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
            .doOnNext {
                appMetric.trackEvent(AppMetricEvent.LoginClick)
            }
            .map(::createLoginParams)
            .flatMapSingle { params ->
                loginUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .updateAnalyticStableParam()
                    .trackEvent(AnalyticsEventType.LOG_IN)
                    .setAppMetricAttribute()
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

    private fun Single<Boolean>.setAppMetricAttribute(): Single<Boolean> =
        this.flatMap { isEmailConfirmed ->
            if (isEmailConfirmed) {
                rxSingle { getProfileUseCase.invoke() }
                    .flatMapObservable { it.asObservable() }
                    .doOnNext {
                        appMetric.setProfileAttributes(it.getMetricAttributes())
                    }
            }
            Single.just(isEmailConfirmed)
        }

    //TODO: need refactor
    private fun checkEmailAndOnBoarding(isEmailConfirmed: Boolean): Single<*> {
        return if (isEmailConfirmed) {
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
                        info.first.isEmailConfirmed != true -> router.navigateTo(ActivateProfile)
                        info.second -> {
                            // fixme Variant A : improved_enabling_location
                            val improvedEnablingLocation = getFeatureConfigUseCase.invoke().improvedEnablingLocation
                            val screen = if (improvedEnablingLocation) Screens.HomeFlow
                            else Screens.HomeFlowVariantA

                            remindersManager.scheduleReminders()
                            router.newRootFlow(screen)
                        }

                        else -> router.newRootFlow(Screens.OnBoardingFlow)
                    }
                }

        } else {
            Single.fromCallable {
                router.navigateTo(ActivateProfile)
            }
        }
    }
}
