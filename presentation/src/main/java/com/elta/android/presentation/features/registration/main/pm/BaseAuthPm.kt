package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.domain.features.auth.interactor.isPasswordValid
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import me.dmdev.rxpm.widget.inputControl

abstract class BaseAuthPm(services: ServiceFacade) : BasePm(services) {

    val emailInput = inputControl(hideErrorOnUserInput = false)
    val passwordInput = inputControl(hideErrorOnUserInput = false)

    val continueEnabledState = State(false)
    val continueAction = Action<Unit>()
    val menuAction = Action<Unit>()

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
            else -> resources.getString(R.string.registration_error_input_password)
        }

}