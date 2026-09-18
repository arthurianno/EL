package com.elta.android.presentation.features.greeting.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.elta.android.presentation.R
import com.elta.android.presentation.features.auth.ui.AuthSubmitButton
import com.elta.android.presentation.features.greeting.model.GreetingState
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes

@Composable
internal fun GreetingScreen(
    state: GreetingState,
    onLogin: () -> Unit,
    onRegistration: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes backgroundRes: Int = R.drawable.img_greetings
) {
    val colors = LocalColors.current
    val types = LocalTypes.current

    Box(modifier.fillMaxSize().background(colors.white)) {
        // The artwork is independent of the foreground and extends behind the buttons.
        AsyncImage(
            model = null,
            fallback = painterResource(backgroundRes),
            error = painterResource(backgroundRes),
            placeholder = painterResource(backgroundRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.matchParentSize()
        )
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val viewportHeight = maxHeight
            val horizontalPadding = if (maxWidth < 340.dp) 20.dp else 27.dp
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = viewportHeight),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    Modifier.fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(colors.white, colors.white.copy(alpha = 0.95f), colors.white.copy(alpha = 0f))
                            )
                        )
                        .padding(start = horizontalPadding, end = horizontalPadding, top = 40.dp, bottom = 32.dp)
                ) {
                    Text(
                        text = state.screenConfig?.title ?: stringResource(R.string.greeting_title),
                        style = types.h0,
                        color = colors.blackBlue
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.screenConfig?.description ?: stringResource(R.string.greeting_message),
                        style = types.subtitle2,
                        color = colors.shadeBlack0
                    )
                }
                Column(
                    Modifier.fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(colors.white.copy(alpha = 0f), colors.white.copy(alpha = 0.85f)))
                        )
                        .padding(start = horizontalPadding, end = horizontalPadding, top = 24.dp, bottom = 16.dp)
                ) {
                    AuthSubmitButton(
                        text = stringResource(R.string.auth_button_continue),
                        enabled = true,
                        isLoading = false,
                        shape = 10,
                        onClick = onLogin
                    )
                    TextButton(
                        onClick = onRegistration,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.greeting_registration),
                            style = types.body1,
                            color = colors.blackBlue
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Preview(showBackground = true, widthDp = 320, heightDp = 480, fontScale = 1.5f, locale = "ru")
@Preview(showBackground = true, widthDp = 812, heightDp = 375, locale = "en")
@Composable
private fun GreetingPreview() {
    EltaTheme {
        GreetingScreen(state = GreetingState(), onLogin = {}, onRegistration = {})
    }
}
