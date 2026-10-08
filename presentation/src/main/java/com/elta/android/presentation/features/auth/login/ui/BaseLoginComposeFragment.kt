package com.elta.android.presentation.features.auth.login.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentLoginComposeBinding
import com.elta.android.presentation.features.auth.login.model.LoginAction
import com.elta.android.presentation.features.auth.login.model.LoginState
import com.elta.android.presentation.features.auth.ui.emailInputError
import com.elta.android.presentation.features.auth.ui.passwordInputError
import com.elta.android.presentation.theme.EltaTheme
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.InputControl

abstract class BaseLoginComposeFragment<PM : BasePm> :
    BaseFragment<PM, FragmentLoginComposeBinding>(FragmentLoginComposeBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_login_compose
    override val statusBarConfigProvider = LightStatusBarConfigProvider
    override val applyBottomSystemInsets = false
    override val applyPlatformSystemWindowFitting = false

    private var uiState by mutableStateOf(LoginState())
    private var backAction: (() -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginComposeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backAction?.invoke()
                }
            }
        )
    }

    override fun onBindPresentationModel(pm: PM) {
        super.onBindPresentationModel(pm)
        pm.progressState.bindTo { uiState = uiState.copy(isLoading = it) }
        pm.screenConfigState.bindTo { uiState = uiState.copy(screenConfig = it) }
        pm.errorControl.dataState.bindTo { updateServerError(pm) }
        pm.errorControl.visibilityState.bindTo { updateServerError(pm) }
    }

    protected fun bindLoginForm(
        emailInput: InputControl,
        passwordInput: InputControl,
        onSubmit: () -> Unit,
        onBack: () -> Unit,
        onForgotPassword: () -> Unit
    ) {
        backAction = onBack
        uiState = uiState.copy(
            email = emailInput.text.value,
            password = passwordInput.text.value
        )

        binding.loginComposeView.setContent {
            EltaTheme {
                LoginScreen(
                    state = uiState,
                    onAction = { action ->
                        when (action) {
                            is LoginAction.EmailChanged -> if (!uiState.isLoading) {
                                val email = action.email.take(254)
                                uiState = uiState.copy(
                                    email = email,
                                    emailError = emailInputError(email, getString(R.string.registration_error_input_email))
                                )
                            }
                            is LoginAction.PasswordChanged -> if (!uiState.isLoading) {
                                uiState = uiState.copy(
                                    password = action.password,
                                    passwordError = passwordInputError(action.password, getString(R.string.registration_password_pattern))
                                )
                            }
                            LoginAction.TogglePasswordVisibility -> {
                                uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
                            }
                            LoginAction.Submit -> if (uiState.canSubmit) {
                                emailInput.textChanges.consumer.accept(uiState.email)
                                passwordInput.textChanges.consumer.accept(uiState.password)
                                onSubmit()
                            }
                        }
                    },
                    onBack = onBack,
                    onForgotPassword = onForgotPassword
                )
            }
        }
    }

    override fun onDestroyView() {
        backAction = null
        uiState = LoginState()
        super.onDestroyView()
    }

    private fun updateServerError(pm: PM) {
        val data = pm.errorControl.dataState.valueOrNull
        uiState = uiState.copy(
            serverError = if (pm.errorControl.visibilityState.valueOrNull == true) {
                data?.description ?: data?.title
            } else {
                null
            }
        )
    }
}
