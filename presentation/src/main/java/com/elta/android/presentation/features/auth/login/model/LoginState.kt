package com.elta.android.presentation.features.auth.login.model

import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.domain.features.auth.interactor.isPasswordValid
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.presentation.core.compose.common.Action

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val serverError: String? = null,
    val isLoading: Boolean = false,
    val screenConfig: ScreenEntity? = null
) {
    val canSubmit: Boolean
        get() = isEmailValid(email) && isPasswordValid(password) && !isLoading
}

sealed interface LoginAction : Action {
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    object TogglePasswordVisibility : LoginAction
    object Submit : LoginAction
}
