package com.elta.android.presentation.features.auth.password.create.pm

import com.elta.android.domain.features.auth.interactor.ResetPasswordUseCase
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.registration.main.pm.BaseAuthPm
import com.elta.android.presentation.messages.SnackbarMessageData
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthPasswordCreatePm @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    services: ServiceFacade
) : BaseAuthPm(services) {

    private val token = State<String>()
    private val passwordChangedSuccessAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        isPasswordValidState.observable
            .subscribe(continueEnabledState.consumer)
            .untilDestroy()

        continueAction.observable
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

        passwordChangedSuccessAction.observable
            .doOnNext(::showSuccessMessage)
            .debounce(CLOSE_SCREEN_TIME_OUT, TimeUnit.SECONDS)
            .doOnNext { router.exit() }
            .subscribe()
            .untilUnbind()
    }

    fun passToken(token: String) {
        this.token.consumer.accept(token)
    }

    private fun createResetPasswordParams(i: Unit): ResetPasswordUseCase.Params =
        ResetPasswordUseCase.Params(token.value, passwordInput.text.value)

    private fun handleSuccess() {
        passwordChangedSuccessAction.consumer.accept(Unit)
    }

    private fun showSuccessMessage(i: Unit) {
        showSnackBarCommand.consumer.accept(
            SnackbarMessageData.SimpleTextMessage(
                resources.getString(R.string.auth_password_create_message_changed_success)
            )
        )
    }

    companion object {
        private const val CLOSE_SCREEN_TIME_OUT = 2L // seconds
    }
}