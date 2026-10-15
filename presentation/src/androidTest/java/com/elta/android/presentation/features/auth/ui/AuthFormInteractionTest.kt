package com.elta.android.presentation.features.auth.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.elta.android.presentation.R
import com.elta.android.presentation.features.auth.login.model.LoginAction
import com.elta.android.presentation.features.auth.login.model.LoginState
import com.elta.android.presentation.features.auth.login.ui.LoginScreen
import com.elta.android.presentation.theme.EltaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class AuthFormInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loginPreservesInputWhenTogglingPasswordAndSubmitsFromIme() {
        val state = mutableStateOf(LoginState())
        var submissions = 0
        compose.setContent {
            EltaTheme {
                LoginScreen(
                    state = state.value,
                    onAction = { action ->
                        when (action) {
                            is LoginAction.EmailChanged -> state.value = state.value.copy(email = action.email)
                            is LoginAction.PasswordChanged -> state.value = state.value.copy(password = action.password)
                            LoginAction.TogglePasswordVisibility ->
                                state.value = state.value.copy(isPasswordVisible = !state.value.isPasswordVisible)
                            LoginAction.Submit -> submissions++
                        }
                    },
                    onBack = {},
                    onForgotPassword = {}
                )
            }
        }
        val fields = compose.onAllNodes(hasSetTextAction())
        fields[0].performClick().performTextInput("user@example.com")
        fields[0].performImeAction()
        fields[1].assertIsFocused().performTextInput("Example123")
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.auth_show_password))
            .performScrollTo().performClick()
        fields[1].assertTextContains("Example123")
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.auth_hide_password))
            .performClick()
        compose.onNode(hasText(compose.activity.getString(R.string.auth_button_continue)) and hasClickAction())
            .assertIsEnabled()
        fields[1].performImeAction()
        compose.runOnIdle {
            assertEquals("user@example.com", state.value.email)
            assertEquals("Example123", state.value.password)
            assertFalse(state.value.isPasswordVisible)
            assertEquals(1, submissions)
        }
    }
}
