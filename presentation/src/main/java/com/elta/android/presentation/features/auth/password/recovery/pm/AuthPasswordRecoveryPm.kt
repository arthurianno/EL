package com.elta.android.presentation.features.auth.password.recovery.pm

import com.elta.android.domain.features.auth.interactor.SendPasswordResetLinkUseCase
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.registration.main.pm.BaseAuthPm
import com.elta.android.presentation.messages.SnackBarMessageData
import javax.inject.Inject

class AuthPasswordRecoveryPm @Inject constructor(
    private val sendPasswordResetLinkUseCase: SendPasswordResetLinkUseCase,
    services: ServiceFacade
) : BaseAuthPm(services) {

    override fun onCreate() {
        super.onCreate()

        isEmailValidState.observable
            .subscribe(continueEnabledState.consumer)
            .untilDestroy()

        continueAction.observable
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

    private fun createSendPasswordRecoveryLinkParams(i: Unit): SendPasswordResetLinkUseCase.Params =
        SendPasswordResetLinkUseCase.Params(emailInput.text.value)

    private fun handleSuccess() {
        showSnackBar(
            SnackBarMessageData.SimpleTextMessage(
                resources.getString(R.string.auth_password_recover_success_message)
            )
        )
    }
}