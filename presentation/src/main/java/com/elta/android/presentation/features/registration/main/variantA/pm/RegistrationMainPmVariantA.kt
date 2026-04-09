package com.elta.android.presentation.features.registration.main.variantA.pm

import android.content.Context
import com.elta.android.domain.features.auth.interactor.RegisterUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.utils.LocaleHelper
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

// fixme Variant A : recovery_account
class RegistrationMainPmVariantA @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val appMetric: AppMetricTracker,
    private val context: Context,
    services: ServiceFacade
) : BaseRegistrationPmVariantA(services) {

    @Suppress("LongMethod")
    override fun onCreate() {
        super.onCreate()

        Observables.combineLatest(
            isEmailValidState.observable,
            isPasswordValidState.observable,
            privacyPolicyAcceptedState.observable
        )
            .map { it.first && it.second && it.third }
            .subscribe(continueEnabledState.consumer)
            .untilDestroy()

        menuAction.observable
            .subscribe { router.startFlow(Screens.AuthFlow) }
            .untilDestroy()

        continueAction.observable
            .skipWhileInProgress()
            .map(::createRegisterParams)
            .flatMapCompletable { params ->
                registerUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun createRegisterParams(i: Unit): RegisterUseCase.Params =
        RegisterUseCase.Params(
            email = emailInput.text.value,
            password = passwordInput.text.value,
            languageTag = LocaleHelper.getLanguage(context),
            countryCode = LocaleHelper.getRegion(context)
        )

    private fun handleSuccess() {
        appMetric.trackEvent(AppMetricEvent.RegistrationContinueClick)
        router.navigateTo(Screens.ActivateProfile)
    }
}
