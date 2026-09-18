package com.elta.android.presentation.features.auth.password.create.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.R
import com.elta.android.presentation.features.auth.password.create.model.PasswordCreateAction
import com.elta.android.presentation.features.auth.password.create.model.PasswordCreateState
import com.elta.android.presentation.features.auth.ui.AuthPasswordField
import com.elta.android.presentation.features.auth.ui.AuthScreenLayout
import com.elta.android.presentation.features.auth.ui.AuthSubmitButton
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes

@Composable
internal fun PasswordCreateScreen(
    state: PasswordCreateState,
    onAction: (PasswordCreateAction) -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalColors.current
    val types = LocalTypes.current
    val focusManager = LocalFocusManager.current
    val submit = {
        if (state.canSubmit) {
            focusManager.clearFocus()
            onAction(PasswordCreateAction.Submit)
        }
    }

    AuthScreenLayout(
        title = state.screenConfig?.title ?: stringResource(R.string.auth_password_create_title),
        subtitle = state.screenConfig?.description ?: stringResource(R.string.auth_password_create_subtitle),
        illustrationRes = R.drawable.img_new_password,
        compactIllustrationSize = DpSize(183.dp, 176.dp),
        topBar = {
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.ic_dialog_close),
                    contentDescription = stringResource(R.string.content_description_close_button),
                    tint = colors.blackBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val formPadding = if (maxWidth < 340.dp) 12.dp else 16.dp
            val buttonPadding = if (maxWidth < 340.dp) 20.dp else 32.dp
            Column(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = formPadding)) {
                    AuthPasswordField(
                        value = state.password,
                        onValueChange = { onAction(PasswordCreateAction.PasswordChanged(it)) },
                        isPasswordVisible = state.isPasswordVisible,
                        onToggleVisibility = { onAction(PasswordCreateAction.TogglePasswordVisibility) },
                        label = stringResource(R.string.auth_password_create_password_hint),
                        helperText = stringResource(R.string.auth_password_create_password_pattern),
                        errorText = state.passwordError?.let { stringResource(it) },
                        enabled = state.hasResetToken && !state.isLoading && !state.isPasswordChanged,
                        onSubmit = submit
                    )
                    if (!state.hasResetToken) {
                        Text(
                            text = stringResource(R.string.auth_password_create_invalid_link),
                            style = types.descriptionError,
                            color = colors.red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                AuthSubmitButton(
                    text = stringResource(R.string.auth_password_create_login_button_title),
                    enabled = state.canSubmit,
                    isLoading = state.isLoading,
                    shape = 10,
                    onClick = submit,
                    modifier = Modifier.padding(horizontal = buttonPadding)
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 375,
    heightDp = 812
)
@Composable
private fun PasswordCreatePreview() {
    EltaTheme {
        PasswordCreateScreen(
            state = PasswordCreateState(
                hasResetToken = true
            ),
            onAction = {},
            onClose = {}
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 320,
    heightDp = 480,
    fontScale = 1.5f
)
@Composable
private fun PasswordCreateErrorPreview() {
    EltaTheme {
        PasswordCreateScreen(
            state = PasswordCreateState(
                password = "short",
                passwordError = R.string.registration_password_pattern,
                hasResetToken = true
            ),
            onAction = {},
            onClose = {}
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 375,
    heightDp = 812
)
@Composable
private fun PasswordCreateLoadingPreview() {
    EltaTheme {
        PasswordCreateScreen(
            state = PasswordCreateState(
                password = "Example123",
                hasResetToken = true,
                isLoading = true
            ),
            onAction = {},
            onClose = {}
        )
    }
}
