package com.elta.android.presentation.features.auth.password.create.model

import androidx.annotation.StringRes
import com.elta.android.domain.features.auth.interactor.isPasswordValid
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.presentation.core.compose.common.Action

data class PasswordCreateState(
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    @StringRes val passwordError: Int? = null,
    val hasResetToken: Boolean = false,
    val isLoading: Boolean = false,
    val isPasswordChanged: Boolean = false,
    val screenConfig: ScreenEntity? = null
) {
    val canSubmit: Boolean
        get() = hasResetToken && isPasswordValid(password) && !isLoading && !isPasswordChanged
}

sealed interface PasswordCreateAction : Action {
    data class PasswordChanged(val password: String) : PasswordCreateAction
    object TogglePasswordVisibility : PasswordCreateAction
    object Submit : PasswordCreateAction
}

sealed interface PasswordCreateEffect {
    object PasswordChanged : PasswordCreateEffect
    object Close : PasswordCreateEffect
    object AuthenticationRequired : PasswordCreateEffect
    data class ShowMessage(@StringRes val resource: Int, val text: String? = null) : PasswordCreateEffect
}
