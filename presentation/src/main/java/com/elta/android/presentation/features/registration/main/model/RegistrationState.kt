package com.elta.android.presentation.features.registration.main.model

import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.domain.features.auth.interactor.isPasswordValid
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.presentation.core.compose.common.Action

data class RegistrationState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isPrivacyPolicyAccepted: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val serverError: String? = null,
    val isLoading: Boolean = false,
    val screenConfig: ScreenEntity? = null
) {
    val canSubmit: Boolean
        get() = isEmailValid(email) && isPasswordValid(password) && isPrivacyPolicyAccepted && !isLoading
}

sealed interface RegistrationAction : Action {
    data class EmailChanged(val email: String) : RegistrationAction
    data class PasswordChanged(val password: String) : RegistrationAction
    data class PrivacyPolicyAcceptedChanged(val accepted: Boolean) : RegistrationAction
    object TogglePasswordVisibility : RegistrationAction
    object PrivacyPolicyClicked : RegistrationAction
    object PersonalDataClicked : RegistrationAction
    object Submit : RegistrationAction
}
