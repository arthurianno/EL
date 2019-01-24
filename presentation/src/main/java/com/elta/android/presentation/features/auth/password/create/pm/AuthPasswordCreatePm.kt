package com.elta.android.presentation.features.auth.password.create.pm

import com.elta.android.domain.features.auth.interactor.ResetPasswordUseCase
import com.elta.android.domain.features.auth.interactor.isPasswordValid
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import me.dmdev.rxpm.widget.inputControl
import timber.log.Timber
import javax.inject.Inject

class AuthPasswordCreatePm @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val passwordInput = inputControl(hideErrorOnUserInput = false)
    val saveButtonEnabledState = State(false)
    val savePasswordAction = Action<Unit>()

    private val token = State<String>()

    override fun onCreate() {
        super.onCreate()

        passwordInput.text.observable
            .map(::validatePassword)
            .map(::getPasswordError)
            .subscribe(passwordInput.error.consumer)
            .untilDestroy()

        passwordInput.text.observable
            .map(::isPasswordValid)
            .subscribe(saveButtonEnabledState.consumer)
            .untilDestroy()

        savePasswordAction.observable
            .skipWhileInProgress()
            .map(::createResetPasswordParams)
            .flatMapCompletable { params ->
                resetPasswordUseCase.execute(params)
                    .bindProgress()
                    .doOnComplete(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    fun passToken(token: String) {
        this.token.consumer.accept(token)
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

    private fun createResetPasswordParams(i: Unit): ResetPasswordUseCase.Params =
        ResetPasswordUseCase.Params(token.value, passwordInput.text.value)

    private fun handleSuccess() {
        Timber.d("handleSuccess for Password reset")
    }
}