package com.elta.android.presentation.features.registration.activation.pm

import com.elta.android.common.errors.NetworkConnectionError
import com.elta.android.domain.features.auth.interactor.CheckEmailUseCase
import com.elta.android.domain.features.auth.interactor.SendConfirmationLinkUseCase
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.messages.SnackBarMessageData
import me.dmdev.rxpm.action
import javax.inject.Inject

class ActivationPm @Inject constructor(
    services: ServiceFacade,
    private val sendConfirmationLinkUseCase: SendConfirmationLinkUseCase,
    private val checkEmailUseCase: CheckEmailUseCase
) : BasePm(services) {

    val sendAgainAction = action<Unit>()
    val continueAction = action<Unit>()

    @Suppress("LongMethod")
    override fun onCreate() {
        super.onCreate()
        sendAgainAction.observable
            .skipWhileInProgress()
            .flatMapCompletable {
                sendConfirmationLinkUseCase.execute()
                    .bindProgress()
                    .doOnComplete(::handleResendSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        continueAction.observable
            .skipWhileInProgress()
            .flatMapSingle {
                checkEmailUseCase.execute()
                    .bindProgress()
                    .doOnSuccess(::handleEmailConfirmed)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun handleResendSuccess() {
        showSnackBar(
            SnackBarMessageData.SimpleTextMessage(
                resources.getString(R.string.registration_email_sent)
            )
        )
    }

    override fun handleError(error: Throwable) {
        if (error is NetworkConnectionError) {
            showSnackBar(
                SnackBarMessageData.SimpleTextMessage(
                    resources.getString(R.string.no_connection_to_the_internet)
                )
            )
        } else {
            super.handleError(error)
        }
    }

    private fun handleEmailConfirmed(isConfirmed: Boolean) {
        when (isConfirmed) {
            true -> router.newRootFlow(Screens.OnBoardingFlow)
            else -> showSnackBar(
                SnackBarMessageData.SimpleTextMessage(
                    resources.getString(R.string.error_verify_your_email)
                )
            )
        }
    }
}
