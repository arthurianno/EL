package com.elta.android.presentation.features.auth.password.recovery.pm

import com.elta.android.domain.features.auth.interactor.SendPasswordResetLinkUseCase
import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.messages.SnackbarMessageData
import me.dmdev.rxpm.widget.inputControl
import javax.inject.Inject

class AuthPasswordRecoveryPm @Inject constructor(
    private val sendPasswordResetLinkUseCase: SendPasswordResetLinkUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val emailInput = inputControl(hideErrorOnUserInput = false)
    val sendButtonEnabledState = State(false)
    val sendAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        emailInput.text.observable
            .map(::validateEmail)
            .map(::getEmailError)
            .subscribe(emailInput.error.consumer)
            .untilDestroy()

        emailInput.text.observable
            .map(::isEmailValid)
            .subscribe(sendButtonEnabledState.consumer)
            .untilDestroy()

        sendAction.observable
            .skipWhileInProgress()
            .map(::createSendPasswordRecoveryLinkParams)
            .flatMapCompletable { params ->
                sendPasswordResetLinkUseCase.execute(params)
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

    private fun createSendPasswordRecoveryLinkParams(i: Unit): SendPasswordResetLinkUseCase.Params =
        SendPasswordResetLinkUseCase.Params(emailInput.text.value)

    private fun handleSuccess() {
        showSnackBar(
            SnackbarMessageData.SimpleTextMessage(
                resources.getString(R.string.auth_password_recover_success_message)
            )
        )
    }
}