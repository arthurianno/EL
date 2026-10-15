package com.elta.android.presentation.features.registration.activation.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.R
import com.elta.android.presentation.features.auth.ui.AuthScreenLayout
import com.elta.android.presentation.features.auth.ui.AuthSubmitButton
import com.elta.android.presentation.features.auth.ui.AuthTypography
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.LocalColors

@Composable
internal fun ActivationScreen(
    isLoading: Boolean,
    onBack: () -> Unit,
    onResend: () -> Unit,
    onCheckActivation: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes illustrationRes: Int = R.drawable.img_active_profile
) {
    val colors = LocalColors.current

    AuthScreenLayout(
        title = stringResource(R.string.registration_activate_title),
        subtitle = stringResource(R.string.registration_activate_description),
        illustrationRes = illustrationRes,
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
            Box(Modifier.weight(1f).padding(end = 4.dp), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onResend, enabled = !isLoading) {
                    Text(
                        text = stringResource(R.string.registration_send_again),
                        style = AuthTypography.toolbar,
                        color = if (isLoading) colors.shadeBlack2 else colors.shadeBlack1,
                        textAlign = TextAlign.End
                    )
                }
            }
        },
        actions = {
            AuthSubmitButton(
                text = stringResource(R.string.registration_activation_check),
                enabled = !isLoading,
                isLoading = isLoading,
                shape = 10,
                onClick = onCheckActivation
            )
        }
    ) {}
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Preview(showBackground = true, widthDp = 320, heightDp = 480, fontScale = 1.5f, locale = "ru")
@Preview(showBackground = true, widthDp = 812, heightDp = 375, locale = "en")
@Composable
private fun ActivationPreview() {
    EltaTheme {
        ActivationScreen(isLoading = false, onBack = {}, onResend = {}, onCheckActivation = {})
    }
}
