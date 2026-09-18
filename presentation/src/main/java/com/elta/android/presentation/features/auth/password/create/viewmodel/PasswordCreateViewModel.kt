package com.elta.android.presentation.features.auth.password.create.viewmodel

import android.content.Context
import com.elta.android.common.errors.InvalidRefreshTokenError
import com.elta.android.common.errors.NetworkConnectionError
import com.elta.android.common.errors.NotFoundError
import com.elta.android.common.errors.ServiceUnavailableError
import com.elta.android.domain.features.auth.interactor.ResetPasswordUseCase
import com.elta.android.domain.features.auth.interactor.isPasswordValid
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.viewmodel.ComposeScreenConfigurable
import com.elta.android.presentation.features.auth.password.create.model.PasswordCreateAction
import com.elta.android.presentation.features.auth.password.create.model.PasswordCreateEffect
import com.elta.android.presentation.features.auth.password.create.model.PasswordCreateState
import io.reactivex.exceptions.CompositeException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.rx2.await

class PasswordCreateViewModel @Inject constructor(
    private val resetPassword: ResetPasswordUseCase,
    override val getScreenConfigUseCase: GetScreenConfigFromCache
) : BaseViewModel<PasswordCreateState>(), ComposeScreenConfigurable {

    override val screenConfigKey = "recovery-password-screen"
    private var isInitialized = false
    private var resetToken: String? = null
    private val effectChannel = Channel<PasswordCreateEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    override fun createInitState() = PasswordCreateState()

    fun setResetToken(token: String?) {
        resetToken = token?.takeIf { it.isNotBlank() }
        reduceState { state.value.copy(hasResetToken = resetToken != null) }
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
            is PasswordCreateAction.PasswordChanged -> {
                if (state.value.isLoading || state.value.isPasswordChanged) return
                reduceState {
                    state.value.copy(
                        password = action.password,
                        passwordError = if (action.password.isEmpty() || isPasswordValid(action.password)) null
                        else R.string.registration_password_pattern
                    )
                }
            }
            PasswordCreateAction.TogglePasswordVisibility -> {
                if (state.value.isLoading || state.value.isPasswordChanged) return
                reduceState { state.value.copy(isPasswordVisible = !state.value.isPasswordVisible) }
            }
            PasswordCreateAction.Submit -> submit()
            else -> super.handleUserAction(action)
        }
    }

    private fun submit() {
        if (!state.value.canSubmit) return
        val token = resetToken ?: return
        val password = state.value.password
        reduceState { state.value.copy(isLoading = true, passwordError = null) }
        launch {
            try {
                resetPassword.execute(ResetPasswordUseCase.Params(token, password)).await()
                resetToken = null
                reduceState {
                    state.value.copy(
                        password = "",
                        isPasswordVisible = false,
                        isPasswordChanged = true,
                        isLoading = false
                    )
                }
                effectChannel.send(PasswordCreateEffect.PasswordChanged)
                delay(CLOSE_SCREEN_DELAY_MILLIS)
                effectChannel.send(PasswordCreateEffect.Close)
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
        val effect = when (cause) {
            is InvalidRefreshTokenError -> PasswordCreateEffect.AuthenticationRequired
            is NotFoundError -> PasswordCreateEffect.ShowMessage(R.string.auth_email_and_password_not_correct)
            is NetworkConnectionError -> PasswordCreateEffect.ShowMessage(R.string.no_connection_to_the_internet)
            is ConnectException, is SocketTimeoutException, is ServiceUnavailableError ->
                PasswordCreateEffect.ShowMessage(R.string.error_server_not_responding_snackbar_message)
            else -> PasswordCreateEffect.ShowMessage(
                R.string.error_general_title,
                cause.message?.takeIf { it.isNotBlank() }
            )
        }
        effectChannel.send(effect)
    }

    private companion object {
        const val CLOSE_SCREEN_DELAY_MILLIS = 2_000L
    }
}
