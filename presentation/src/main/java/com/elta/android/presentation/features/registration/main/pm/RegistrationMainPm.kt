package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.domain.features.auth.interactor.RegisterUseCase
import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.domain.features.auth.interactor.isPasswordValid
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.widget.inputControl
import javax.inject.Inject

class RegistrationMainPm @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    services: ServiceFacade
) : BasePm(services) {

    private val privacyPolicyAcceptedState = State<Boolean>()

    val emailInput = inputControl(hideErrorOnUserInput = false)
    val passwordInput = inputControl(hideErrorOnUserInput = false)
    val privacyPolicyAcceptAction = Action<Boolean>()

    val privacyPolicyClickAction = Action<Unit>()
    val openPrivacyPolicyCommand = Command<Unit>()
    val continueEnabledState = State(false)
    val continueAction = Action<Unit>()
    val userHasAccountAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        emailInput.text.observable
            .map(::validateEmail)
            .map(::getEmailError)
            .subscribe(emailInput.error.consumer)
            .untilDestroy()

        passwordInput.text.observable
            .map(::validatePassword)
            .map(::getPasswordError)
            .subscribe(passwordInput.error.consumer)
            .untilDestroy()

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

        userHasAccountAction.observable
            .subscribe {
                flowRouter?.startFlow(Screens.AuthFlow)
            }
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

    private fun validateEmail(email: String): Boolean =
        when (email.isEmpty()) {
            true -> true
            else -> isEmailValid(email)
        }

    private fun getEmailError(isEmailValid: Boolean): String =
        when (isEmailValid) {
            true -> ""
            else -> resources.getString(R.string.registration_error_input_email)
        }

    private fun validatePassword(password: String): Boolean =
        when (password.isEmpty()) {
            true -> true
            else -> isPasswordValid(password)
        }

    private fun getPasswordError(isEmailValid: Boolean): String =
        when (isEmailValid) {
            true -> ""
            else -> resources.getString(R.string.registration_error_input_email)
        }

    private fun createRegisterParams(i: Unit): RegisterUseCase.Params =
        RegisterUseCase.Params(emailInput.text.value, passwordInput.text.value)

    private fun handleSuccess() {
        router.navigateTo(Screens.ActivateProfile)
    }
}