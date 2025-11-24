package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.common.errors.ProfileIsDeletedError
import com.elta.android.domain.features.auth.interactor.RegisterUseCase
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.States
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.pm.ServiceFacade
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class RegistrationMainPm @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val appMetric: AppMetricTracker,
    services: ServiceFacade
) : BaseRegistrationPm(services) {

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

    override fun handleError(error: Throwable) {
        when(error){
            is ProfileIsDeletedError -> {
                setErrorStateData(
                    States.SimpleError(
                        icon = R.drawable.ic_warning,
                        description = resources.getString(R.string.registration_error_email_already_registered)
                    )
                )
                setErrorViewVisibility(true)
            }
            else -> super.handleError(error)
        }
    }

    private fun createRegisterParams(i: Unit): RegisterUseCase.Params =
        RegisterUseCase.Params(emailInput.text.value, passwordInput.text.value)

    private fun handleSuccess() {
        appMetric.trackEvent(AppMetricEvent.RegistrationContinueClick)
        router.navigateTo(Screens.ActivateProfile)
    }
}
