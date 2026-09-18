package com.elta.android.presentation.features.auth.login.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.R
import com.elta.android.presentation.features.auth.login.model.LoginAction
import com.elta.android.presentation.features.auth.login.model.LoginState
import com.elta.android.presentation.features.auth.ui.AuthEmailField
import com.elta.android.presentation.features.auth.ui.AuthPasswordField
import com.elta.android.presentation.features.auth.ui.AuthScreenLayout
import com.elta.android.presentation.features.auth.ui.AuthSubmitButton
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes

@Composable
internal fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val focusManager = LocalFocusManager.current
    val submit = {
        if (state.canSubmit) {
            focusManager.clearFocus()
            onAction(LoginAction.Submit)
        }
    }

    AuthScreenLayout(
        title = state.screenConfig?.title ?: stringResource(R.string.auth_title),
        subtitle = state.screenConfig?.description ?: stringResource(R.string.auth_subtitle),
        illustrationRes = R.drawable.img_login,
        compactIllustrationSize = DpSize(155.dp, 149.dp),
        illustrationUrl = null,
        modifier = modifier,
        topBar = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = stringResource(R.string.content_description_back_button),
                    tint = colors.blackBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onForgotPassword, enabled = !state.isLoading) {
                Text(
                    text = stringResource(R.string.auth_toolbar_button_text),
                    style = LocalTypes.current.body1,
                    color = if (state.isLoading) colors.shadeBlack2 else colors.blackBlue
                )
            }
        }
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            AuthEmailField(
                value = state.email,
                onValueChange = { onAction(LoginAction.EmailChanged(it)) },
                label = stringResource(R.string.registration_main_email_hint),
                error = state.emailError,
                enabled = !state.isLoading,
                imeAction = ImeAction.Next,
                onSubmit = { focusManager.moveFocus(FocusDirection.Next) }
            )
            Spacer(Modifier.height(16.dp))
            AuthPasswordField(
                value = state.password,
                onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
                isPasswordVisible = state.isPasswordVisible,
                onToggleVisibility = { onAction(LoginAction.TogglePasswordVisibility) },
                label = stringResource(R.string.registration_main_password_hint),
                helperText = stringResource(R.string.registration_password_pattern),
                errorText = state.passwordError,
                enabled = !state.isLoading,
                onSubmit = submit
            )
            state.serverError?.let { message ->
                Text(
                    text = message,
                    style = LocalTypes.current.descriptionError,
                    color = colors.red,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        AuthSubmitButton(
            text = stringResource(R.string.auth_button_continue),
            enabled = state.canSubmit,
            isLoading = state.isLoading,
            shape = 10,
            onClick = submit,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun LoginPreview() {
    LoginStatePreview(LoginState())
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun LoginFilledPreview() {
    LoginStatePreview(LoginState(email = "user@example.com", password = "Example123"))
}

@Preview(showBackground = true, widthDp = 320, heightDp = 480, fontScale = 1.5f, locale = "ru")
@Composable
private fun LoginErrorPreview() {
    LoginStatePreview(
        LoginState(
            email = "user@",
            password = "short",
            emailError = stringResource(R.string.registration_error_input_email),
            passwordError = stringResource(R.string.registration_password_pattern)
        )
    )
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun LoginLoadingPreview() {
    LoginStatePreview(
        LoginState(email = "user@example.com", password = "Example123", isLoading = true)
    )
}

@Composable
private fun LoginStatePreview(state: LoginState) {
    EltaTheme {
        LoginScreen(state = state, onAction = {}, onBack = {}, onForgotPassword = {})
    }
}
