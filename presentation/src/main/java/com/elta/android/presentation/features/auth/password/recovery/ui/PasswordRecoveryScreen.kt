package com.elta.android.presentation.features.auth.password.recovery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.elta.android.presentation.R
import com.elta.android.presentation.features.auth.password.recovery.model.PasswordRecoveryAction
import com.elta.android.presentation.features.auth.password.recovery.model.PasswordRecoveryState
import com.elta.android.presentation.features.auth.ui.AuthEmailField
import com.elta.android.presentation.features.auth.ui.AuthSubmitButton
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes

@Composable
internal fun PasswordRecoveryScreen(
    state: PasswordRecoveryState,
    onAction: (PasswordRecoveryAction) -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalColors.current
    val types = LocalTypes.current
    val focusManager = LocalFocusManager.current

    val submit = {
        if (state.canSubmit) {
            focusManager.clearFocus()
            onAction(PasswordRecoveryAction.Submit)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.white)
            .safeDrawingPadding()
            .imePadding()
    ) {

        /*
         * Макет Figma сделан под 375dp.
         *
         * На обычном телефоне:
         * titlePadding = 27dp
         *
         * На очень узком экране:
         * немного уменьшаем padding.
         *
         * Это НЕ масштабирование всего дизайна,
         * а только адаптация контейнера.
         */
        val titleHorizontalPadding = when {
            maxWidth < 340.dp -> 16.dp
            maxWidth < 360.dp -> 20.dp
            else -> 27.dp
        }

        /*
         * Нижняя форма в Figma визуально шире блока title,
         * поэтому у неё отдельный padding.
         */
        val formHorizontalPadding = when {
            maxWidth < 340.dp -> 12.dp
            else -> 16.dp
        }

        /*
         * Кнопка чуть уже формы.
         */
        val buttonHorizontalPadding = when {
            maxWidth < 340.dp -> 20.dp
            else -> 27.dp
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            /*
             * HEADER
             *
             * Отдельный блок.
             * Он НЕ должен наследовать padding заголовка.
             */
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onClose
                ) {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_dialog_close
                        ),
                        contentDescription = stringResource(
                            R.string.content_description_close_button
                        ),
                        tint = colors.blackBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            /*
             * MAIN CONTENT
             *
             * Сам Column БЕЗ horizontal padding.
             * Это принципиально важно.
             */
            Column(
                modifier = Modifier
                    .weight(1f)
                   // .verticalScroll(rememberScrollState())
            ) {

                /*
                 * TITLE BLOCK
                 *
                 * Figma:
                 * X ≈ 27
                 */
                Column(
                    modifier = Modifier.padding(
                        horizontal = titleHorizontalPadding
                    )
                ) {
                    Text(
                        text = state.screenConfig?.title
                            ?: stringResource(
                                R.string.auth_password_recovery_title
                            ),
                        style = types.h0,
                        color = colors.blackBlue
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = state.screenConfig?.description
                            ?: stringResource(
                                R.string.auth_password_recovery_subtitle
                            ),
                        style = types.subtitle2,
                        color = colors.shadeBlack0
                    )
                }

                /*
                 * HERO IMAGE
                 *
                 * Figma:
                 *
                 * X = 0
                 * W = 375
                 * H = 360
                 *
                 * Поэтому здесь НЕТ horizontal padding.
                 */
                AsyncImage(
                    model = null,
                    fallback = painterResource(
                        R.drawable.img_password_recovery
                    ),
                    error = painterResource(
                        R.drawable.img_password_recovery
                    ),
                    placeholder = painterResource(
                        R.drawable.img_password_recovery
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(375f / 360f)
                )

                /*
                 * EMAIL
                 *
                 * У формы собственный padding.
                 */
                Column(
                    modifier = Modifier.padding(
                        horizontal = formHorizontalPadding
                    )
                ) {

                    AuthEmailField(
                        value = state.email,
                        onValueChange = {
                            onAction(
                                PasswordRecoveryAction.EmailChanged(it)
                            )
                        },
                        label = stringResource(
                            R.string.auth_password_recovery_email_hint
                        ),
                        error = state.emailError?.let {
                            stringResource(it)
                        },
                        enabled = !state.isLoading &&
                                !state.isLinkSent,
                        onSubmit = submit
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(11.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                R.drawable.ic_info
                            ),
                            contentDescription = null,
                            tint = colors.shadeBlack2,
                            modifier = Modifier.size(22.dp)
                        )

                        Text(
                            text = stringResource(
                                R.string.auth_password_recovery_email_description
                            ),
                            style = types.caption1,
                            color = colors.shadeBlack2,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                /*
                 * BUTTON
                 *
                 * Отдельный padding.
                 * Поэтому она может быть уже email-поля.
                 */
                AuthSubmitButton(
                    text = stringResource(
                        R.string.auth_password_recovery_button_text
                    ),
                    enabled = state.canSubmit,
                    isLoading = state.isLoading,
                    shape = 10,
                    onClick = submit,
                    modifier = Modifier
                        .padding(
                            horizontal = buttonHorizontalPadding
                        )
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