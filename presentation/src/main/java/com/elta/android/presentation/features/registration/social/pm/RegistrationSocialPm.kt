package com.elta.android.presentation.features.registration.social.pm

import com.elta.android.common.errors.SocialNetworkAlreadyRegisteredError
import com.elta.android.domain.features.auth.interactor.GetSocialUserUseCase
import com.elta.android.domain.features.auth.interactor.RegisterWithSocialNetworkUseCase
import com.elta.android.domain.features.auth.model.SocialUser
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.registration.main.pm.BaseRegistrationPm
import com.elta.android.presentation.messages.SnackBarMessageData
import io.reactivex.rxkotlin.Observables
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RegistrationSocialPm @Inject constructor(
    private val registerWithSocialNetworkUseCase: RegisterWithSocialNetworkUseCase,
    private val getSocialUserUseCase: GetSocialUserUseCase,
    services: ServiceFacade
) : BaseRegistrationPm(services) {

    val authTitleState = State(resources.getString(R.string.registration_social_title_no_name))

    private val getSocialUserAction = Action<SocialNetworkType>()
    private val socialNetworkState = State<SocialNetworkType>()
    private val showErrorAndContinueAction = Action<String>()

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

        showErrorAndContinueAction.observable
            .map { SnackBarMessageData.SimpleTextMessage(it) }
            .doOnNext(::showSnackBar)
            .delay(DELAY, TimeUnit.MILLISECONDS)
            .doOnNext { handleSuccess() }
            .subscribe()
            .untilDestroy()
    }

    fun setSocialNetwork(network: SocialNetworkType) {
        socialNetworkState.consumer.accept(network)
    }

    override fun handleError(error: Throwable) {
        if (error is SocialNetworkAlreadyRegisteredError)
            showErrorAndContinueAction.consumer.accept(error.message ?: "")
        else
            super.handleError(error)
    }

    private fun createRegisterParams(i: Unit): RegisterWithSocialNetworkUseCase.Params =
        RegisterWithSocialNetworkUseCase.Params(
            emailInput.text.value,
            passwordInput.text.value,
            socialNetworkState.value
        )

    private fun createSocialUserParams(network: SocialNetworkType): GetSocialUserUseCase.Params =
        GetSocialUserUseCase.Params(network)

    private fun handleSuccess() {
        router.navigateTo(Screens.ActivateProfile)
    }

    private fun handleSocialUserSuccess(user: SocialUser) {
        authTitleState.consumer.accept(resources.getString(R.string.registration_social_title, user.name))
    }

    companion object {
        private const val DELAY = 2000L // millis
    }
}