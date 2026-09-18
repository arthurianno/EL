package com.elta.android.presentation.features.registration.main.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.R
import com.elta.android.presentation.features.auth.ui.AuthEmailField
import com.elta.android.presentation.features.auth.ui.AuthPasswordField
import com.elta.android.presentation.features.auth.ui.AuthScreenLayout
import com.elta.android.presentation.features.auth.ui.AuthSubmitButton
import com.elta.android.presentation.features.registration.main.model.RegistrationAction
import com.elta.android.presentation.features.registration.main.model.RegistrationState
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes

@Composable
internal fun RegistrationScreen(
    state: RegistrationState,
    onAction: (RegistrationAction) -> Unit,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val types = LocalTypes.current
    val focusManager = LocalFocusManager.current
    val submit = {
        if (state.canSubmit) {
            focusManager.clearFocus()
            onAction(RegistrationAction.Submit)
        }
    }

    AuthScreenLayout(
        title = state.screenConfig?.title ?: stringResource(R.string.registration_main_title_new_user),
        subtitle = state.screenConfig?.description ?: stringResource(R.string.registration_main_subtitle),
        illustrationRes = R.drawable.img_registration,
        illustrationUrl = null,
        compactIllustrationSize = DpSize(125.dp, 120.dp),
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
            TextButton(onClick = onLogin, enabled = !state.isLoading) {
                Text(
                    text = stringResource(R.string.registration_main_toolbar_button_text),
                    style = types.body1,
                    color = if (state.isLoading) colors.shadeBlack2 else colors.blackBlue
                )
            }
        }
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            AuthEmailField(
                value = state.email,
                onValueChange = { onAction(RegistrationAction.EmailChanged(it)) },
                label = stringResource(R.string.registration_main_email_hint),
                error = state.emailError,
                enabled = !state.isLoading,
                imeAction = ImeAction.Next,
                onSubmit = { focusManager.moveFocus(FocusDirection.Next) }
            )
            Spacer(Modifier.height(16.dp))
            AuthPasswordField(
                value = state.password,
                onValueChange = { onAction(RegistrationAction.PasswordChanged(it)) },
                isPasswordVisible = state.isPasswordVisible,
                onToggleVisibility = { onAction(RegistrationAction.TogglePasswordVisibility) },
                label = stringResource(R.string.registration_main_password_hint),
                helperText = stringResource(R.string.registration_password_pattern),
                errorText = state.passwordError,
                enabled = !state.isLoading,
                onSubmit = submit
            )
            state.serverError?.let { message ->
                Text(
                    text = message,
                    style = types.descriptionError,
                    color = colors.red,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            RegistrationConsent(
                accepted = state.isPrivacyPolicyAccepted,
                enabled = !state.isLoading,
                onAction = onAction
            )
        }
        Spacer(Modifier.height(24.dp))
        AuthSubmitButton(
            text = stringResource(R.string.registration_main_button_continue),
            enabled = state.canSubmit,
            isLoading = state.isLoading,
            shape = 10,
            onClick = submit,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun RegistrationConsent(
    accepted: Boolean,
    enabled: Boolean,
    onAction: (RegistrationAction) -> Unit
) {
    val colors = LocalColors.current
    val text = registrationConsentText(
        description = stringResource(R.string.registration_main_description_privacy_policy),
        privacyPolicyLabel = stringResource(R.string.registration_main_privacy_policy_clickable_mask),
        personalDataLabel = stringResource(R.string.registration_main_personal_data_clickable_mask),
        linkStyles = TextLinkStyles(
            style = SpanStyle(color = colors.gGreenB, textDecoration = TextDecoration.Underline)
        ),
        onAction = onAction
    )
    Row(verticalAlignment = Alignment.Top) {
        Checkbox(
            checked = accepted,
            onCheckedChange = { onAction(RegistrationAction.PrivacyPolicyAcceptedChanged(it)) },
            enabled = enabled,
            colors = CheckboxDefaults.colors(checkedColor = colors.gGreenB),
            modifier = Modifier.semantics { contentDescription = text.text }
        )
        Text(
            text = text,
            style = LocalTypes.current.caption1,
            color = colors.shadeBlack2,
            modifier = Modifier.weight(1f).padding(top = 12.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun RegistrationPreview() {
    RegistrationStatePreview(RegistrationState())
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun RegistrationFilledPreview() {
    RegistrationStatePreview(
        RegistrationState(email = "user@example.com", password = "Example123", isPrivacyPolicyAccepted = true)
    )
}

@Preview(showBackground = true, widthDp = 320, heightDp = 480, fontScale = 1.5f, locale = "ru")
@Composable
private fun RegistrationErrorPreview() {
    RegistrationStatePreview(
        RegistrationState(
            email = "user@example.com",
            password = "Example123",
            isPrivacyPolicyAccepted = true,
            serverError = stringResource(R.string.registration_error_email_already_registered)
        )
    )
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812, locale = "en")
@Composable
private fun RegistrationLoadingPreview() {
    RegistrationStatePreview(
        RegistrationState(
            email = "user@example.com", password = "Example123",
            isPrivacyPolicyAccepted = true, isLoading = true
        )
    )
}

@Composable
private fun RegistrationStatePreview(state: RegistrationState) {
    EltaTheme {
        RegistrationScreen(state = state, onAction = {}, onBack = {}, onLogin = {})
    }
}
