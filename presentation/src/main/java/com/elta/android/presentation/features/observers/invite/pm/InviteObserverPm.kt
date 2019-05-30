package com.elta.android.presentation.features.observers.invite.pm

import com.elta.android.common.errors.CantSendInviteToYourselfError
import com.elta.android.common.errors.EmailAlreadyInvitedError
import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.domain.features.observers.interactor.SendObserverInviteUseCase
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.States
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.messages.SnackBarMessageData
import me.dmdev.rxpm.widget.inputControl
import javax.inject.Inject

class InviteObserverPm @Inject constructor(
    private val sendObserverInviteUseCase: SendObserverInviteUseCase,
    services: ServiceFacade
) : BasePm(services) {

    val emailInput = inputControl(hideErrorOnUserInput = false)
    val continueEnabledState = State(false)
    val continueAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        emailInput.text.observable
            .map(::validateEmail)
            .map(::getEmailError)
            .subscribe(emailInput.error.consumer)
            .untilDestroy()

        continueAction.observable
            .skipWhileInProgress()
            .map(::createObserverInviteUseCaseParams)
            .flatMapCompletable {
                sendObserverInviteUseCase.execute(it)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    override fun handleError(error: Throwable) {
        when (error) {
            is EmailAlreadyInvitedError -> {
                setErrorStateData(States.SimpleError(icon = R.drawable.ic_warning, description = error.message))
                setErrorViewVisibility(true)
            }
            is CantSendInviteToYourselfError -> {
                setErrorStateData(States.SimpleError(icon = R.drawable.ic_warning, description = error.message))
                setErrorViewVisibility(true)
            }
            else -> super.handleError(error)
        }
    }

    private fun validateEmail(email: String): Boolean =
        when (email.isEmpty()) {
            true -> true
            else -> isEmailValid(email).also {
                continueEnabledState.consumer.accept(it)
            }
        }

    private fun getEmailError(isEmailValid: Boolean): String =
        when (isEmailValid) {
            true -> ""
            else -> resources.getString(R.string.registration_error_input_email)
        }

    private fun handleSuccess() {
        hideKeyBoardCommand.consumer.accept(Unit)
        bus.event(Events.ObserverInvited)
        showSnackBar(
            SnackBarMessageData.SimpleTextMessage(
                resources.getString(R.string.profile_observer_invite_with_success)
            )
        )
        router.exit()
    }

    private fun createObserverInviteUseCaseParams(i: Unit) =
        SendObserverInviteUseCase.Params(emailInput.text.value)
}