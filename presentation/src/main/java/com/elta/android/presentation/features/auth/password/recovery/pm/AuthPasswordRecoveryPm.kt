package com.elta.android.presentation.features.auth.password.recovery.pm

import android.content.Context
import android.util.Log
import coil.imageLoader
import com.elta.android.common.errors.NotFoundError
import com.elta.android.domain.features.auth.interactor.SendPasswordResetLinkUseCase
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.ScreenConfigurable
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.registration.main.pm.BaseAuthPm
import com.elta.android.presentation.messages.SnackBarMessageData

import javax.inject.Inject

class AuthPasswordRecoveryPm @Inject constructor(
    private val sendPasswordResetLinkUseCase: SendPasswordResetLinkUseCase,
    private val getFeatureConfigUseCase: GetFeatureConfigUseCase,
    private val getScreenConfigFromCache: GetScreenConfigFromCache,
    private val context: Context,
    services: ServiceFacade
) : BaseAuthPm(services), ScreenConfigurable {

    override val screenConfigKey = "password-screen"
    override val getScreenConfigUseCase = getScreenConfigFromCache

    override fun onCreate() {
        super.onCreate()
        loadScreenConfig(context)
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
                resources.getString(R.string.registration_email_sent)
            )
        )

        // fixme Variant A : recovery_account
        val isNewRecovery = getFeatureConfigUseCase.invoke().recoveryAccount
        val screen = if (isNewRecovery) Screens.Login
        else Screens.LoginVariantA
        router.navigateTo(screen)
    }

    override fun handleError(error: Throwable) {
        if (error is NotFoundError) {
            emailInput.error.consumer.accept(resources.getString(R.string.user_not_registered))
        } else {
            super.handleError(error)
        }
    }
}
