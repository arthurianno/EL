package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.domain.features.auth.interactor.LoginToSocialNetworkUseCase
import com.elta.android.domain.features.auth.interactor.RegisterUseCase
import com.elta.android.domain.features.auth.model.SocialNetwork
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.ServiceFacade
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class RegistrationMainPm @Inject constructor(
    private val loginToSocialNetworkUseCase: LoginToSocialNetworkUseCase,
    private val registerUseCase: RegisterUseCase,
    services: ServiceFacade
) : BaseSocialPm(services) {

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

        socialAction.observable
            .skipWhileInProgress()
            .map(::createLoginSocialParams)
            .flatMapCompletable { params ->
                loginToSocialNetworkUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete { handleSocialNetworkLoggedIn(params.network) }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun createRegisterParams(i: Unit): RegisterUseCase.Params =
        RegisterUseCase.Params(emailInput.text.value, passwordInput.text.value)

    private fun createLoginSocialParams(network: SocialNetwork): LoginToSocialNetworkUseCase.Params =
        LoginToSocialNetworkUseCase.Params(network)

    private fun handleSuccess() {
        router.navigateTo(Screens.ActivateProfile)
    }

    private fun handleSocialNetworkLoggedIn(network: SocialNetwork) {
        router.navigateTo(Screens.RegistrationSocial(network))
    }
}