package com.elta.android.presentation.features.auth.login.pm

import com.elta.android.domain.features.auth.interactor.LoginUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.registration.main.pm.BaseAuthPm
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class LoginPm @Inject constructor(
    private val loginUseCase: LoginUseCase,
    services: ServiceFacade
) : BaseAuthPm(services) {

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
            .subscribe { router.navigateTo(Screens.PasswordRecovery) }
            .untilDestroy()

        continueAction.observable
            .skipWhileInProgress()
            .map(::createLoginParams)
            .flatMapSingle { params ->
                loginUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnSuccess(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun createLoginParams(i: Unit): LoginUseCase.Params =
        LoginUseCase.Params(emailInput.text.value, passwordInput.text.value)

    private fun handleSuccess(isEmailActivated: Boolean) {
        when (isEmailActivated) {
            true -> flowRouter?.newRootFlow(Screens.OnBoardingFlow)
            else -> router.navigateTo(Screens.ActivateProfile)
        }
    }
}