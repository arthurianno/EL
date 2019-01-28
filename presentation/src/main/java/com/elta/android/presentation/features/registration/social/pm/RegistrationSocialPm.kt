package com.elta.android.presentation.features.registration.social.pm

import com.elta.android.domain.features.auth.interactor.GetSocialUserUseCase
import com.elta.android.domain.features.auth.interactor.RegisterWithSocialNetworkUseCase
import com.elta.android.domain.features.auth.model.SocialNetwork
import com.elta.android.domain.features.auth.model.SocialUser
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.registration.main.pm.BaseRegistrationPm
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class RegistrationSocialPm @Inject constructor(
    private val registerWithSocialNetworkUseCase: RegisterWithSocialNetworkUseCase,
    private val getSocialUserUseCase: GetSocialUserUseCase,
    services: ServiceFacade
) : BaseRegistrationPm(services) {

    val authTitleState = State(resources.getString(R.string.registration_social_title_no_name))

    private val getSocialUserAction = Action<SocialNetwork>()
    private val socialNetworkState = State<SocialNetwork>()

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
            .subscribe { flowRouter?.startFlow(Screens.AuthFlow) }
            .untilDestroy()

        continueAction.observable
            .skipWhileInProgress()
            .map(::createRegisterParams)
            .flatMapCompletable { params ->
                registerWithSocialNetworkUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        getSocialUserAction.observable
            .map(::createSocialUserParams)
            .flatMapSingle { params ->
                getSocialUserUseCase.execute(params)
                    .doOnSuccess(::handleSocialUserSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        socialNetworkState.observable
            .take(1)
            .subscribe(getSocialUserAction.consumer)
            .untilDestroy()
    }

    fun setSocialNetwork(network: SocialNetwork) {
        socialNetworkState.consumer.accept(network)
    }

    private fun createRegisterParams(i: Unit): RegisterWithSocialNetworkUseCase.Params =
        RegisterWithSocialNetworkUseCase.Params(
            emailInput.text.value,
            passwordInput.text.value,
            socialNetworkState.value
        )

    private fun createSocialUserParams(network: SocialNetwork): GetSocialUserUseCase.Params =
        GetSocialUserUseCase.Params(network)

    private fun handleSuccess() {
        router.navigateTo(Screens.ActivateProfile)
    }

    private fun handleSocialUserSuccess(user: SocialUser) {
        authTitleState.consumer.accept(resources.getString(R.string.registration_social_title, user.name))
    }
}