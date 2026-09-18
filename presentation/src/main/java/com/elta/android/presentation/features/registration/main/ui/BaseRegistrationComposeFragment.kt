package com.elta.android.presentation.features.registration.main.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentRegistrationComposeBinding
import com.elta.android.presentation.features.registration.main.model.RegistrationAction
import com.elta.android.presentation.features.registration.main.model.RegistrationState
import com.elta.android.presentation.features.registration.policy.ui.RegistrationPrivacyPolicyFragment
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.utils.hideKeyboardFun
import com.nullgr.core.ui.fragments.showDialog
import me.dmdev.rxpm.State
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.InputControl

abstract class BaseRegistrationComposeFragment<PM : BasePm> :
    BaseFragment<PM, FragmentRegistrationComposeBinding>(FragmentRegistrationComposeBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_registration_compose
    override val statusBarConfigProvider = LightStatusBarConfigProvider
    override val applyBottomSystemInsets = false
    override val applyPlatformSystemWindowFitting = false

    private var uiState by mutableStateOf(RegistrationState())
    private var backAction: (() -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registrationComposeView.setViewCompositionStrategy(
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

    protected fun bindRegistrationForm(
        emailInput: InputControl,
        passwordInput: InputControl,
        consentState: State<Boolean>,
        onConsentChanged: (Boolean) -> Unit,
        onSubmit: () -> Unit,
        onBack: () -> Unit,
        onLogin: () -> Unit,
        onPrivacyPolicy: () -> Unit,
        onPersonalData: () -> Unit
    ) {
        backAction = onBack
        emailInput.text.bindTo { uiState = uiState.copy(email = it) }
        emailInput.error.bindTo { uiState = uiState.copy(emailError = it.takeIf(String::isNotEmpty)) }
        passwordInput.text.bindTo { uiState = uiState.copy(password = it) }
        passwordInput.error.bindTo { uiState = uiState.copy(passwordError = it.takeIf(String::isNotEmpty)) }
        consentState.bindTo { uiState = uiState.copy(isPrivacyPolicyAccepted = it) }

        binding.registrationComposeView.setContent {
            EltaTheme {
                RegistrationScreen(
                    state = uiState,
                    onAction = { action ->
                        if (!uiState.isLoading) {
                            when (action) {
                                is RegistrationAction.EmailChanged ->
                                    emailInput.textChanges.consumer.accept(action.email.take(254))
                                is RegistrationAction.PasswordChanged ->
                                    passwordInput.textChanges.consumer.accept(action.password)
                                is RegistrationAction.PrivacyPolicyAcceptedChanged ->
                                    onConsentChanged(action.accepted)
                                RegistrationAction.TogglePasswordVisibility ->
                                    uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
                                RegistrationAction.PrivacyPolicyClicked -> onPrivacyPolicy()
                                RegistrationAction.PersonalDataClicked -> onPersonalData()
                                RegistrationAction.Submit -> if (uiState.canSubmit) onSubmit()
                            }
                        }
                    },
                    onBack = onBack,
                    onLogin = onLogin
                )
            }
        }
    }

    protected fun showDocument(@StringRes urlResource: Int) {
        view?.hideKeyboardFun()
        childFragmentManager.showDialog(
            RegistrationPrivacyPolicyFragment.newInstance(getString(urlResource))
        )
    }

    override fun onDestroyView() {
        backAction = null
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
