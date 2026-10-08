package com.elta.android.presentation.features.registration.main.viewmodel

import android.content.Context
import androidx.annotation.StringRes
import com.elta.android.common.errors.EmailAlreadyRegisteredError
import com.elta.android.common.errors.IncorrectLoginOrPasswordError
import com.elta.android.common.errors.NetworkConnectionError
import com.elta.android.common.errors.NotFoundError
import com.elta.android.common.errors.ProfileIsDeletedError
import com.elta.android.common.errors.ServiceUnavailableError
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.viewmodel.ComposeScreenConfigurable
import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.features.auth.ui.emailInputError
import com.elta.android.presentation.features.auth.ui.passwordInputError
import com.elta.android.presentation.features.registration.main.model.RegistrationAction
import com.elta.android.presentation.features.registration.main.model.RegistrationDialog
import com.elta.android.presentation.features.registration.main.model.RegistrationMode
import com.elta.android.presentation.features.registration.main.model.RegistrationState
import io.reactivex.exceptions.CompositeException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber

sealed interface RegistrationEffect {
    data class ShowDocument(@StringRes val urlResource: Int) : RegistrationEffect
    data class ShowMessage(val message: String) : RegistrationEffect
}

class RegistrationViewModel @Inject constructor(
    private val session: RegistrationSession,
    private val telemetry: RegistrationTelemetry,
    private val context: Context,
    override val getScreenConfigUseCase: GetScreenConfigFromCache
) : BaseViewModel<RegistrationState>(), ComposeScreenConfigurable {
    override val screenConfigKey = "registration-screens"

    private var initialized = false
    private var mode = RegistrationMode.Recoverable
    private val effectChannel = Channel<RegistrationEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    override fun createInitState() = RegistrationState()

    fun initialize(mode: RegistrationMode) {
        if (initialized) return
        initialized = true
        this.mode = mode
        if (mode == RegistrationMode.Recoverable) {
            loadScreenConfig(context.applicationContext) { config, _ ->
                state.value.copy(screenConfig = config)
            }
        }
    }

    fun onAction(action: RegistrationAction) {
        if (state.value.isLoading) return
        when (action) {
            is RegistrationAction.EmailChanged -> {
                val email = action.email.take(254)
                reduceState {
                    state.value.copy(
                        email = email,
                        emailError = emailInputError(email, context.getString(R.string.registration_error_input_email)),
                        serverError = null
                    )
                }
            }
            is RegistrationAction.PasswordChanged -> reduceState {
                state.value.copy(
                    password = action.password,
                    passwordError = passwordInputError(
                        action.password, context.getString(R.string.registration_password_pattern)
                    ),
                    serverError = null
                )
            }
            is RegistrationAction.PrivacyPolicyAcceptedChanged -> reduceState {
                state.value.copy(isPrivacyPolicyAccepted = action.accepted)
            }
            RegistrationAction.TogglePasswordVisibility -> reduceState {
                state.value.copy(isPasswordVisible = !state.value.isPasswordVisible)
            }
            RegistrationAction.PrivacyPolicyClicked -> {
                telemetry.privacyPolicyOpened()
                effectChannel.trySend(RegistrationEffect.ShowDocument(R.string.registration_privacy_policy))
            }
            RegistrationAction.PersonalDataClicked ->
                effectChannel.trySend(RegistrationEffect.ShowDocument(R.string.registration_personal_data))
            RegistrationAction.Submit -> submit()
        }
    }

    override fun backClick() {
        router.navigateTo(Screens.GreetingFlow)
    }

    fun loginClick() {
        if (!state.value.isLoading) (router as FlowRouter).startFlow(Screens.AuthFlow)
    }

    fun dismissDialog() {
        reduceState { state.value.copy(dialog = null) }
    }

    fun contactSupport() {
        if (state.value.dialog != RegistrationDialog.ProfileDeleted) return
        dismissDialog()
        router.navigateTo(
            Screens.EmailScreen(
                email = context.getString(R.string.profile_support_email),
                body = context.getString(R.string.profile_restore_email_body)
            )
        )
    }

    private fun submit() {
        if (!state.value.canSubmit) return
        val email = state.value.email
        val password = state.value.password
        reduceState { state.value.copy(isLoading = true, serverError = null) }
        launch {
            try {
                session.register(email, password)
                router.navigateTo(Screens.ActivateProfile)
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
            is ProfileIsDeletedError -> if (mode == RegistrationMode.Legacy) {
                reduceState { state.value.copy(dialog = RegistrationDialog.ProfileDeleted) }
            } else {
                reduceState {
                    state.value.copy(serverError = context.getString(R.string.registration_error_email_already_registered))
                }
            }
            is NotFoundError -> reduceState {
                state.value.copy(serverError = context.getString(R.string.auth_email_and_password_not_correct))
            }
            is EmailAlreadyRegisteredError, is IncorrectLoginOrPasswordError -> reduceState {
                state.value.copy(serverError = cause.message ?: context.getString(R.string.error_general_title))
            }
            is NetworkConnectionError -> showMessage(R.string.no_connection_to_the_internet)
            is ConnectException, is SocketTimeoutException, is ServiceUnavailableError ->
                showMessage(R.string.error_server_not_responding_snackbar_message)
            else -> effectChannel.trySend(
                RegistrationEffect.ShowMessage(cause.message ?: context.getString(R.string.error_general_title))
            )
        }
    }

    private fun showMessage(@StringRes resource: Int) {
        effectChannel.trySend(RegistrationEffect.ShowMessage(context.getString(resource)))
    }
}
