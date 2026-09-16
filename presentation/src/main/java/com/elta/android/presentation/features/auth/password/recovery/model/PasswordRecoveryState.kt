package com.elta.android.presentation.features.auth.password.recovery.model

import androidx.annotation.StringRes
import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.presentation.core.compose.common.Action

data class PasswordRecoveryState(
    val email: String = "",
    @StringRes val emailError: Int? = null,
    val isLoading: Boolean = false,
    val isLinkSent: Boolean = false,
    val screenConfig: ScreenEntity? = null
) {
    val canSubmit: Boolean
        get() = email.isNotEmpty() && isEmailValid(email) && !isLoading && !isLinkSent
}

sealed interface PasswordRecoveryAction : Action {
    data class EmailChanged(val email: String) : PasswordRecoveryAction
    object Submit : PasswordRecoveryAction
}

sealed interface PasswordRecoveryEffect {
    data class LinkSent(val useNewLogin: Boolean) : PasswordRecoveryEffect
    data class ShowMessage(@StringRes val resource: Int, val text: String? = null) : PasswordRecoveryEffect
    object AuthenticationRequired : PasswordRecoveryEffect
}
