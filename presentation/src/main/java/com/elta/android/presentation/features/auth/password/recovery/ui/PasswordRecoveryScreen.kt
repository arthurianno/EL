package com.elta.android.presentation.features.auth.password.recovery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.R
import com.elta.android.presentation.features.auth.password.recovery.model.PasswordRecoveryAction
import com.elta.android.presentation.features.auth.password.recovery.model.PasswordRecoveryState
import com.elta.android.presentation.features.auth.ui.AuthEmailField
import com.elta.android.presentation.features.auth.ui.AuthScreenLayout
import com.elta.android.presentation.features.auth.ui.AuthSubmitButton
import com.elta.android.presentation.features.auth.ui.AuthTypography
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.LocalColors

@Composable
internal fun PasswordRecoveryScreen(
    state: PasswordRecoveryState,
    onAction: (PasswordRecoveryAction) -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalColors.current
    val focusManager = LocalFocusManager.current
    val submit = {
        if (state.canSubmit) {
            focusManager.clearFocus()
            onAction(PasswordRecoveryAction.Submit)
        }
    }

    AuthScreenLayout(
        title = state.screenConfig?.title ?: stringResource(R.string.auth_password_recovery_title),
        subtitle = state.screenConfig?.description ?: stringResource(R.string.auth_password_recovery_subtitle),
        illustrationRes = R.drawable.img_password_recovery,
        compactIllustrationSize = DpSize(125.dp, 120.dp),
        actionSpacing = 16.dp,
        topBar = {
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.ic_dialog_close),
                    contentDescription = stringResource(R.string.content_description_close_button),
                    tint = colors.blackBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        actions = {
            AuthSubmitButton(
                text = stringResource(R.string.auth_password_recovery_button_text),
                enabled = state.canSubmit,
                isLoading = state.isLoading,
                shape = 10,
                onClick = submit
            )
        }
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            AuthEmailField(
                value = state.email,
                onValueChange = { onAction(PasswordRecoveryAction.EmailChanged(it)) },
                label = stringResource(R.string.auth_password_recovery_email_hint),
                error = state.emailError?.let { stringResource(it) },
                enabled = !state.isLoading && !state.isLinkSent,
                onSubmit = submit
            )
            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = null,
                    tint = colors.shadeBlack2,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = stringResource(R.string.auth_password_recovery_email_description),
                    style = AuthTypography.helper,
                    color = colors.shadeBlack2,
                    modifier = Modifier.weight(1f)
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
private fun PasswordRecoveryPreview() {
    EltaTheme {
        PasswordRecoveryScreen(
            state = PasswordRecoveryState(),
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
private fun PasswordRecoverySmallPreview() {
    EltaTheme {
        PasswordRecoveryScreen(
            state = PasswordRecoveryState(
                email = "user@example.com",
                emailError = R.string.user_not_registered
            ),
            onAction = {},
            onClose = {}
        )
    }
}
