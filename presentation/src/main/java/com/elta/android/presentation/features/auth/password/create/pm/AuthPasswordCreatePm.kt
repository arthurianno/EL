package com.elta.android.presentation.features.auth.password.create.pm

import com.elta.android.domain.features.auth.interactor.ResetPasswordUseCase
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.registration.main.pm.BaseAuthPm
import timber.log.Timber
import javax.inject.Inject

class AuthPasswordCreatePm @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    services: ServiceFacade
) : BaseAuthPm(services) {

    private val token = State<String>()

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
    }

    fun passToken(token: String) {
        this.token.consumer.accept(token)
    }

    private fun createResetPasswordParams(i: Unit): ResetPasswordUseCase.Params =
        ResetPasswordUseCase.Params(token.value, passwordInput.text.value)

    private fun handleSuccess() {
        Timber.d("handleSuccess for Password reset")
    }
}