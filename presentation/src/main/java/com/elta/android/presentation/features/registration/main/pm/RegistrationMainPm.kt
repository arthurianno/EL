package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.domain.features.auth.interactor.RegisterUseCase
import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.domain.features.auth.interactor.isPasswordValid
import com.elta.android.presentation.R
import com.elta.android.presentation.States
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.state_view.StateData
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.widget.inputControl
import javax.inject.Inject

class RegistrationMainPm @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    services: ServiceFacade
) : BasePm(services) {

    private val privacyPolicyAcceptedState = State<Boolean>()
    private val emailError: StateData by lazy {
        States.SimpleError(icon = R.drawable.ic_warning, description = resources.getString(R.string.server_error_email_exists))
    }
    private var v: Boolean = false

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

        errorControl.dataState.consumer.accept(emailError)
        userHasAccountAction.observable
            .subscribe {
                v = !v
                errorControl.visibilityState.consumer.accept(v)
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

    }
}