package com.elta.android.presentation.features.auth.password.recovery.viewmodel

import android.content.Context
import com.elta.android.common.errors.InvalidRefreshTokenError
import com.elta.android.common.errors.NetworkConnectionError
import com.elta.android.common.errors.NotFoundError
import com.elta.android.common.errors.ServiceUnavailableError
import com.elta.android.domain.features.auth.interactor.SendPasswordResetLinkUseCase
import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.viewmodel.ComposeScreenConfigurable
import com.elta.android.presentation.features.auth.password.recovery.model.PasswordRecoveryAction
import com.elta.android.presentation.features.auth.password.recovery.model.PasswordRecoveryEffect
import com.elta.android.presentation.features.auth.password.recovery.model.PasswordRecoveryState
import io.reactivex.exceptions.CompositeException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.rx2.await

class PasswordRecoveryViewModel @Inject constructor(
    private val sendPasswordResetLink: SendPasswordResetLinkUseCase,
    private val getFeatureConfig: GetFeatureConfigUseCase,
    override val getScreenConfigUseCase: GetScreenConfigFromCache
) : BaseViewModel<PasswordRecoveryState>(), ComposeScreenConfigurable {

    override val screenConfigKey = "password-screen"
    private var isInitialized = false
    private val effectChannel = Channel<PasswordRecoveryEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    override fun createInitState() = PasswordRecoveryState()

    fun onLinkSentHandled() {
        reduceState { state.value.copy(isLinkSent = false) }
    }

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true
        loadScreenConfig(context.applicationContext) { config, _ ->
            state.value.copy(screenConfig = config)
        }
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            is PasswordRecoveryAction.EmailChanged -> {
                if (state.value.isLoading || state.value.isLinkSent) return
                reduceState {
                    state.value.copy(
                        email = action.email,
                        emailError = if (action.email.isEmpty() || isEmailValid(action.email)) null
                        else R.string.registration_error_input_email
                    )
                }
            }
            PasswordRecoveryAction.Submit -> submit()
            else -> super.handleUserAction(action)
        }
    }

    private fun submit() {
        if (!state.value.canSubmit) return
        val email = state.value.email
        // Set synchronously so a second tap or IME action cannot start another request.
        reduceState { state.value.copy(isLoading = true, emailError = null) }
        launch {
            try {
                sendPasswordResetLink.execute(SendPasswordResetLinkUseCase.Params(email)).await()
                val useNewLogin = getFeatureConfig().recoveryAccount
                reduceState { state.value.copy(isLinkSent = true) }
                effectChannel.send(PasswordRecoveryEffect.LinkSent(useNewLogin))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                showRequestError(error)
            } finally {
                reduceState { state.value.copy(isLoading = false) }
            }
        }
    }

    private suspend fun showRequestError(error: Throwable) {
        val cause = if (error is CompositeException) error.exceptions.lastOrNull() ?: error else error
        when (cause) {
            is NotFoundError -> reduceState { state.value.copy(emailError = R.string.user_not_registered) }
            is InvalidRefreshTokenError -> effectChannel.send(PasswordRecoveryEffect.AuthenticationRequired)
            is NetworkConnectionError -> effectChannel.send(
                PasswordRecoveryEffect.ShowMessage(R.string.no_connection_to_the_internet)
            )
            is ConnectException, is SocketTimeoutException, is ServiceUnavailableError -> effectChannel.send(
                PasswordRecoveryEffect.ShowMessage(R.string.error_server_not_responding_snackbar_message)
            )
            else -> effectChannel.send(
                PasswordRecoveryEffect.ShowMessage(R.string.error_general_title, cause.message?.takeIf { it.isNotBlank() })
            )
        }
    }
}
