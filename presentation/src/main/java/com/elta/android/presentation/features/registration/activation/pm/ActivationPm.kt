package com.elta.android.presentation.features.registration.activation.pm

import com.elta.android.domain.features.auth.interactor.CheckEmailUseCase
import com.elta.android.domain.features.auth.interactor.SendConfirmationLinkUseCase
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.messages.SnackBarMessageData
import me.dmdev.rxpm.action
import javax.inject.Inject

class ActivationPm @Inject constructor(
    private val sendConfirmationLinkUseCase: SendConfirmationLinkUseCase,
    private val checkEmailUseCase: CheckEmailUseCase,
    private val appMetric: AppMetricTracker,
    services: ServiceFacade
) : BasePm(services) {

    val sendAgainAction = action<Unit>()
    val continueAction = action<Unit>()

    @Suppress("LongMethod")
    override fun onCreate() {
        super.onCreate()
        appMetric.trackEvent(AppMetricEvent.ProfileActivationScreen)
        sendAgainAction.observable
            .skipWhileInProgress()
            .doOnNext {
                appMetric.trackEvent(AppMetricEvent.SendLetter)
            }
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
            .doOnNext {
                appMetric.trackEvent(AppMetricEvent.ActivationContinue)
            }
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

    private fun handleEmailConfirmed(isConfirmed: Boolean) {
        when (isConfirmed) {
            true -> router.newRootFlow(Screens.OnBoardingFlow)
            else -> bus.event(Events.EmailNotConfirmed)
        }
    }
}
