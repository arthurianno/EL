package com.elta.android.presentation.features.registration.activation.viewmodel

import android.content.Context
import com.elta.android.common.errors.InvalidRefreshTokenError
import com.elta.android.common.errors.ServiceUnavailableError
import com.elta.android.domain.features.auth.interactor.CheckEmailUseCase
import com.elta.android.domain.features.auth.interactor.SendConfirmationLinkUseCase
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.features.registration.activation.model.ActivationState
import com.nullgr.core.rx.RxBus
import io.reactivex.exceptions.CompositeException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.rx2.await
import timber.log.Timber

sealed interface ActivationEffect {
    data class ShowMessage(val message: String) : ActivationEffect
}

class ActivationViewModel @Inject constructor(
    private val sendConfirmationLink: SendConfirmationLinkUseCase,
    private val checkEmail: CheckEmailUseCase,
    private val appMetric: AppMetricTracker,
    private val bus: RxBus,
    private val context: Context
) : BaseViewModel<ActivationState>() {
    private val effectChannel = Channel<ActivationEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    init {
        appMetric.trackEvent(AppMetricEvent.ProfileActivationScreen)
    }

    override fun createInitState() = ActivationState()

    fun resend() {
        if (state.value.isLoading) return
        appMetric.trackEvent(AppMetricEvent.SendLetter)
        reduceState { state.value.copy(isLoading = true) }
        launch {
            try {
                sendConfirmationLink.execute().await()
                showMessage(context.getString(R.string.registration_email_sent))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                if (error is Error) throw error
                showError(error)
            } finally {
                reduceState { state.value.copy(isLoading = false) }
            }
        }
    }

    fun checkActivation() {
        if (state.value.isLoading) return
        appMetric.trackEvent(AppMetricEvent.ActivationContinue)
        reduceState { state.value.copy(isLoading = true) }
        launch {
            try {
                if (checkEmail.execute().await()) {
                    (router as FlowRouter).newRootFlow(Screens.OnBoardingFlow)
                } else {
                    bus.event(Events.EmailNotConfirmed)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                if (error is Error) throw error
                showError(error)
            } finally {
                reduceState { state.value.copy(isLoading = false) }
            }
        }
    }

    private fun showError(error: Throwable) {
        val cause = if (error is CompositeException) error.exceptions.lastOrNull() ?: error else error
        Timber.e(cause)
        when (cause) {
            is InvalidRefreshTokenError -> (router as FlowRouter).newRootFlow(Screens.AuthFlow)
            is ConnectException, is SocketTimeoutException, is ServiceUnavailableError ->
                showMessage(context.getString(R.string.error_server_not_responding_snackbar_message))
            else -> showMessage(cause.message ?: context.getString(R.string.error_general_title))
        }
    }

    private fun showMessage(message: String) {
        effectChannel.trySend(ActivationEffect.ShowMessage(message))
    }
}
