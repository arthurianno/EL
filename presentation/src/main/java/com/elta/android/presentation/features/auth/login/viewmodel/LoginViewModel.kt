package com.elta.android.presentation.features.auth.login.viewmodel

import android.content.Context
import com.elta.android.common.errors.EmailAlreadyRegisteredError
import com.elta.android.common.errors.IncorrectLoginOrPasswordError
import com.elta.android.common.errors.InvalidRefreshTokenError
import com.elta.android.common.errors.NetworkConnectionError
import com.elta.android.common.errors.NotFoundError
import com.elta.android.common.errors.ProfileIsDeletedError
import com.elta.android.common.errors.ServiceUnavailableError
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.analytics.Analytics
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.viewmodel.ComposeScreenConfigurable
import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.features.auth.login.model.LoginAction
import com.elta.android.presentation.features.auth.login.model.LoginDialog
import com.elta.android.presentation.features.auth.login.model.LoginMode
import com.elta.android.presentation.features.auth.login.model.LoginState
import com.elta.android.presentation.features.auth.ui.emailInputError
import com.elta.android.presentation.features.auth.ui.passwordInputError
import com.elta.android.presentation.features.profile.settings.reminders.utils.RemindersManager
import io.reactivex.exceptions.CompositeException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber

sealed interface LoginEffect {
    data class ShowMessage(val message: String) : LoginEffect
}

class LoginViewModel @Inject constructor(
    private val session: LoginSession,
    private val analytics: Analytics,
    private val appMetric: AppMetricTracker,
    private val remindersManager: RemindersManager,
    private val context: Context,
    override val getScreenConfigUseCase: GetScreenConfigFromCache
) : BaseViewModel<LoginState>(), ComposeScreenConfigurable {
    override val screenConfigKey = "login-screen"

    private var initialized = false
    private var mode = LoginMode.Recoverable
    private var restoredEmailActivated: Boolean? = null
    private val effectChannel = Channel<LoginEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    override fun createInitState() = LoginState()

    fun initialize(mode: LoginMode) {
        if (initialized) return
        initialized = true
        this.mode = mode
        if (mode == LoginMode.Recoverable) {
            loadScreenConfig(context.applicationContext) { config, _ ->
                state.value.copy(screenConfig = config)
            }
        }
    }

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> {
                if (state.value.isLoading) return
                val email = action.email.take(254)
                reduceState {
                    state.value.copy(
                        email = email,
                        emailError = emailInputError(
                            email, context.getString(R.string.registration_error_input_email)
                        ),
                        serverError = null
                    )
                }
            }
            is LoginAction.PasswordChanged -> {
                if (state.value.isLoading) return
                reduceState {
                    state.value.copy(
                        password = action.password,
                        passwordError = passwordInputError(
                            action.password, context.getString(R.string.registration_password_pattern)
                        ),
                        serverError = null
                    )
                }
            }
            LoginAction.TogglePasswordVisibility ->
                reduceState { state.value.copy(isPasswordVisible = !state.value.isPasswordVisible) }
            LoginAction.Submit -> submit()
        }
    }

    override fun backClick() {
        router.navigateTo(Screens.GreetingFlow)
    }

    fun forgotPassword() {
        if (state.value.isLoading) return
        analytics.trackEvent(AnalyticsEvent(AnalyticsEventType.PASSWORD_RECOVERY))
        router.navigateTo(Screens.PasswordRecovery)
    }

    fun onDialogPositive() {
        when (state.value.dialog) {
            LoginDialog.ProfileDeleted -> {
                reduceState { state.value.copy(dialog = null) }
                if (mode == LoginMode.Recoverable) restoreProfile()
            }
            LoginDialog.ProfileRestored -> {
                val isActivated = restoredEmailActivated ?: return
                restoredEmailActivated = null
                reduceState { state.value.copy(dialog = null) }
                completeLogin(isActivated)
            }
            null -> Unit
        }
    }

    fun onDialogNegative() {
        val sendSupportEmail = state.value.dialog == LoginDialog.ProfileDeleted && mode == LoginMode.Legacy
        reduceState { state.value.copy(dialog = null) }
        if (sendSupportEmail) {
            router.navigateTo(
                Screens.EmailScreen(
                    email = context.getString(R.string.profile_support_email),
                    body = context.getString(R.string.profile_restore_email_body)
                )
            )
        }
    }

    private fun submit() {
        if (!state.value.canSubmit) return
        val email = state.value.email
        val password = state.value.password
        appMetric.trackEvent(AppMetricEvent.LoginClick)
        reduceState { state.value.copy(isLoading = true, serverError = null) }
        launch {
            try {
                navigate(session.complete(session.authenticate(email, password)))
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

    private fun restoreProfile() {
        val email = state.value.email
        val password = state.value.password
        reduceState { state.value.copy(isLoading = true, serverError = null) }
        launch {
            try {
                restoredEmailActivated = session.restore(email, password)
                reduceState { state.value.copy(dialog = LoginDialog.ProfileRestored) }
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

    private fun completeLogin(isActivated: Boolean) {
        if (state.value.isLoading) return
        reduceState { state.value.copy(isLoading = true) }
        launch {
            try {
                navigate(session.complete(isActivated))
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

    private fun navigate(destination: LoginDestination) {
        when (destination) {
            LoginDestination.Activate -> router.navigateTo(Screens.ActivateProfile)
            LoginDestination.Onboarding -> (router as FlowRouter).newRootFlow(Screens.OnBoardingFlow)
            LoginDestination.Home, LoginDestination.HomeVariantA -> {
                remindersManager.scheduleReminders()
                (router as FlowRouter).newRootFlow(
                    if (destination == LoginDestination.Home) Screens.HomeFlow else Screens.HomeFlowVariantA
                )
            }
        }
    }

    private fun showError(error: Throwable) {
        val cause = if (error is CompositeException) error.exceptions.lastOrNull() ?: error else error
        Timber.e(cause)
        when (cause) {
            is ProfileIsDeletedError -> reduceState {
                state.value.copy(dialog = LoginDialog.ProfileDeleted)
            }
            is NotFoundError -> reduceState {
                state.value.copy(serverError = context.getString(R.string.auth_email_and_password_not_correct))
            }
            is EmailAlreadyRegisteredError, is IncorrectLoginOrPasswordError -> reduceState {
                state.value.copy(serverError = cause.message ?: context.getString(R.string.auth_email_and_password_not_correct))
            }
            is InvalidRefreshTokenError -> (router as FlowRouter).newRootFlow(Screens.AuthFlow)
            is NetworkConnectionError -> showMessage(R.string.no_connection_to_the_internet)
            is ConnectException, is SocketTimeoutException, is ServiceUnavailableError ->
                showMessage(R.string.error_server_not_responding_snackbar_message)
            else -> effectChannel.trySend(
                LoginEffect.ShowMessage(cause.message ?: context.getString(R.string.error_general_title))
            )
        }
    }

    private fun showMessage(resource: Int) {
        effectChannel.trySend(LoginEffect.ShowMessage(context.getString(resource)))
    }
}
