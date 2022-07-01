package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.common.errors.EmailAlreadyRegisteredError
import com.elta.android.common.errors.IncorrectLoginOrPasswordError
import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.domain.features.auth.interactor.isPasswordValid
import com.elta.android.presentation.R
import com.elta.android.presentation.States
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.inputControl

abstract class BaseAuthPm(services: ServiceFacade) : BasePm(services) {

    val emailInput = inputControl(hideErrorOnUserInput = false)
    val passwordInput = inputControl(hideErrorOnUserInput = false)

    val isEmailValidState = state(false)
    val isPasswordValidState = state(false)

    val continueEnabledState = state(false)
    val continueAction = action<Unit>()
    val menuAction = action<Unit>()

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
    }

    override fun handleError(error: Throwable) {
        when (error is EmailAlreadyRegisteredError || error is IncorrectLoginOrPasswordError) {
            true -> {
                setErrorStateData(
                    States.SimpleError(
                        icon = R.drawable.ic_warning,
                        description = error.message
                    )
                )
                setErrorViewVisibility(true)
            }
            else -> super.handleError(error)
        }
    }

    private fun validateEmail(email: String): Boolean =
        when (email.isEmpty()) {
            true -> true
            else -> isEmailValid(email).also {
                isEmailValidState.consumer.accept(it)
            }
        }

    private fun getEmailError(isEmailValid: Boolean): String =
        when (isEmailValid) {
            true -> ""
            else -> resources.getString(R.string.registration_error_input_email)
        }

    private fun validatePassword(password: String): Boolean =
        when (password.isEmpty()) {
            true -> true
            else -> isPasswordValid(password).also {
                isPasswordValidState.consumer.accept(it)
            }
        }

    private fun getPasswordError(isEmailValid: Boolean): String =
        when (isEmailValid) {
            true -> ""
            else -> resources.getString(R.string.registration_password_pattern)
        }
}
