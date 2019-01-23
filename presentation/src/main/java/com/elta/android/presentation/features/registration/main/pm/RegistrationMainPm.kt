package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.domain.features.auth.interactor.RegisterUseCase
import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.domain.features.auth.interactor.isPasswordValid
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.ServiceFacade
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class RegistrationMainPm @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    services: ServiceFacade
) : BaseAuthPm(services) {

    private val privacyPolicyAcceptedState = State<Boolean>()

    val privacyPolicyAcceptAction = Action<Boolean>()
    val privacyPolicyClickAction = Action<Unit>()
    val openPrivacyPolicyCommand = Command<Unit>()

    override fun onCreate() {
        super.onCreate()

        privacyPolicyClickAction.observable
            .subscribe(openPrivacyPolicyCommand.consumer)
            .untilDestroy()

        privacyPolicyAcceptAction.observable
            .subscribe(privacyPolicyAcceptedState.consumer)
            .untilDestroy()

        Observables.combineLatest(
            emailInput.text.observable.map(::isEmailValid),
            passwordInput.text.observable.map(::isPasswordValid),
            privacyPolicyAcceptedState.observable
        )
            .map { it.first && it.second && it.third }
            .subscribe(continueEnabledState.consumer)
            .untilDestroy()

        menuAction.observable
            .subscribe { flowRouter?.startFlow(Screens.AuthFlow) }
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
        RegisterUseCase.Params(emailInput.text.value, passwordInput.text.value)

    private fun handleSuccess() {
        router.navigateTo(Screens.ActivateProfile)
    }
}