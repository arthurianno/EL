package com.elta.android.presentation.features.registration.confirmation.viewmodel

import com.elta.android.common.errors.EmailLinkInvalid
import com.elta.android.common.errors.InvalidRefreshTokenError
import com.elta.android.common.errors.ServiceUnavailableError
import com.elta.android.domain.features.auth.interactor.CheckTokenOwnerUseCase
import com.elta.android.domain.features.auth.interactor.ConfirmEmailUseCase
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.features.registration.confirmation.model.ConfirmationStatus
import com.elta.android.presentation.features.registration.confirmation.model.EmailConfirmationState
import com.nullgr.core.rx.RxBus
import io.reactivex.exceptions.CompositeException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx2.await
import timber.log.Timber

class EmailConfirmationViewModel @Inject constructor(
    private val confirmEmail: ConfirmEmailUseCase,
    private val checkTokenOwner: CheckTokenOwnerUseCase,
    private val bus: RxBus
) : BaseViewModel<EmailConfirmationState>() {
    private var token: String? = null

    override fun createInitState() = EmailConfirmationState()

    fun initialize(token: String) {
        if (this.token != null) return
        this.token = token
        confirm(token)
    }

    fun retry() {
        val token = token ?: return
        if (state.value.status == ConfirmationStatus.Loading) return
        confirm(token)
    }

    fun continueToPreviousScreen() {
        router.exit()
    }

    fun loginWithAnotherAccount() {
        router.backTo(Screens.GreetingFlow)
    }

    private fun confirm(token: String) {
        reduceState { EmailConfirmationState(ConfirmationStatus.Loading) }
        launch {
            try {
                confirmEmail.execute(ConfirmEmailUseCase.Params(token)).await()
                val isOwner = checkTokenOwner.execute(CheckTokenOwnerUseCase.Params(token)).await()
                if (isOwner) {
                    bus.event(Events.ProfileDataChanged)
                    (router as FlowRouter).newRootFlow(Screens.OnBoardingFlow)
                } else {
                    reduceState { EmailConfirmationState(ConfirmationStatus.OtherAccount) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                if (error is Error) throw error
                showError(error)
            }
        }
    }

    private fun showError(error: Throwable) {
        val cause = if (error is CompositeException) error.exceptions.lastOrNull() ?: error else error
        Timber.e(cause)
        when (cause) {
            is EmailLinkInvalid -> router.newRootChain(Screens.GreetingFlow, Screens.AuthFlow)
            is InvalidRefreshTokenError -> (router as FlowRouter).newRootFlow(Screens.AuthFlow)
            else -> reduceState {
                EmailConfirmationState(
                    ConfirmationStatus.Failure(
                        message = cause.message,
                        isServerUnavailable = cause is ConnectException ||
                            cause is SocketTimeoutException || cause is ServiceUnavailableError
                    )
                )
            }
        }
    }
}
