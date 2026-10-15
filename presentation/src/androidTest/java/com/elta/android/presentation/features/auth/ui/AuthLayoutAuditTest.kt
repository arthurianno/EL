package com.elta.android.presentation.features.auth.ui

import android.graphics.Bitmap
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.elta.android.presentation.R
import com.elta.android.presentation.features.auth.login.model.LoginState
import com.elta.android.presentation.features.auth.login.ui.LoginScreen
import com.elta.android.presentation.features.auth.password.create.model.PasswordCreateState
import com.elta.android.presentation.features.auth.password.create.ui.PasswordCreateScreen
import com.elta.android.presentation.features.auth.password.recovery.model.PasswordRecoveryState
import com.elta.android.presentation.features.auth.password.recovery.ui.PasswordRecoveryScreen
import com.elta.android.presentation.features.greeting.model.GreetingState
import com.elta.android.presentation.features.greeting.ui.GreetingScreen
import com.elta.android.presentation.features.registration.activation.ui.ActivationScreen
import com.elta.android.presentation.features.registration.main.model.RegistrationState
import com.elta.android.presentation.features.registration.main.ui.RegistrationScreen
import com.elta.android.presentation.theme.EltaTheme
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Captures current geometry for design review; does not assert visual parity with Figma. */
class AuthLayoutAuditTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun captureAuthStatesAndGeometry() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val arguments = InstrumentationRegistry.getArguments()
        val fontScale = arguments.getString("auditFontScale", "1.0").toFloat()
        val mode = arguments.getString("auditMode", "baseline")
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "auth-audit/$mode")
        directory.mkdirs()
        val scene = mutableStateOf("login")
        lateinit var focusManager: FocusManager
        compose.runOnUiThread {
            compose.activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        val emailError = compose.activity.getString(R.string.registration_error_input_email)
        val passwordError = compose.activity.getString(R.string.registration_password_pattern)
        compose.setContent {
            focusManager = LocalFocusManager.current
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                EltaTheme {
                    when (scene.value) {
                        "login", "login_keyboard" -> LoginScreen(LoginState(), {}, {}, {})
                        "login_errors" -> LoginScreen(LoginState(email = "user@", password = "short", emailError = emailError, passwordError = passwordError), {}, {}, {})
                        "registration", "registration_keyboard" -> RegistrationScreen(RegistrationState(), {}, {}, {})
                        "registration_errors" -> RegistrationScreen(RegistrationState(email = "user@", password = "short", emailError = emailError, passwordError = passwordError), {}, {}, {})
                        "activation" -> ActivationScreen(false, {}, {}, {})
                        "recovery", "recovery_keyboard" -> PasswordRecoveryScreen(PasswordRecoveryState(), {}, {})
                        "recovery_error" -> PasswordRecoveryScreen(PasswordRecoveryState(email = "user@", emailError = R.string.registration_error_input_email), {}, {})
                        "new_password", "new_password_keyboard" -> PasswordCreateScreen(PasswordCreateState(hasResetToken = true), {}, {})
                        "new_password_error" -> PasswordCreateScreen(PasswordCreateState(password = "short", passwordError = R.string.registration_password_pattern, hasResetToken = true), {}, {})
                        "greeting" -> GreetingScreen(GreetingState(), {}, {})
                    }
                }
            }
        }
        val reports = JSONArray()
        val scenes = listOf("login", "registration", "activation", "recovery", "new_password", "greeting",
            "login_errors", "registration_errors", "recovery_error", "new_password_error",
            "login_keyboard", "registration_keyboard", "recovery_keyboard", "new_password_keyboard")
        scenes.forEach { name ->
            compose.runOnIdle { focusManager.clearFocus(force = true) }
            compose.runOnUiThread {
                WindowInsetsControllerCompat(compose.activity.window, compose.activity.window.decorView)
                    .hide(WindowInsetsCompat.Type.ime())
            }
            SystemClock.sleep(350)
            compose.runOnIdle { scene.value = name }
            compose.waitForIdle()
            if (name.endsWith("_keyboard")) {
                compose.onAllNodes(hasSetTextAction())[0].performClick()
                compose.waitUntil(5000) {
                    var visible = false
                    compose.runOnUiThread {
                        visible = ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                            ?.isVisible(WindowInsetsCompat.Type.ime()) == true
                    }
                    visible
                }
            }
            compose.waitForIdle()
            SystemClock.sleep(450)
            val labels = JSONArray()
            val density = compose.activity.resources.displayMetrics.density
            compose.onAllNodes(hasText("", substring = true), useUnmergedTree = true).fetchSemanticsNodes().forEach { node ->
                val bounds = node.boundsInWindow
                labels.put(JSONObject().put("semantics", node.config.toString())
                    .put("leftDp", bounds.left / density).put("topDp", bounds.top / density)
                    .put("widthDp", bounds.width / density).put("heightDp", bounds.height / density))
            }
            val insets = ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
            val report = JSONObject().put("scene", name).put("fontScale", fontScale)
                .put("statusBarDp", (insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0) / density)
                .put("navigationBarDp", (insets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0) / density)
                .put("imeDp", (insets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0) / density)
                .put("textNodes", labels)
            val screenshot = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
            File(directory, "$name.png").outputStream().use { screenshot.compress(Bitmap.CompressFormat.PNG, 100, it) }
            assertTrue(File(directory, "$name.png").length() > 0)
            if (name.endsWith("_keyboard")) {
                val submitResource = when {
                    name.startsWith("registration") -> R.string.registration_main_button_continue
                    name.startsWith("recovery") -> R.string.auth_password_recovery_button_text
                    name.startsWith("new_password") -> R.string.auth_password_create_button_continue
                    else -> R.string.auth_button_continue
                }
                val submit = compose.onNode(hasText(compose.activity.getString(submitResource)) and hasClickAction())
                submit.assertIsDisplayed()
                val bounds = submit.fetchSemanticsNode().boundsInWindow
                val windowHeight = compose.activity.window.decorView.height
                val imeHeight = insets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
                assertTrue("$name: submit must stay above the IME", bounds.bottom <= windowHeight - imeHeight)
                report.put("submitVisibleInitially", true)
                report.put("submitBottomDp", bounds.bottom / density)
                val fields = compose.onAllNodes(hasSetTextAction())
                fields[fields.fetchSemanticsNodes().lastIndex].performScrollTo().assertIsDisplayed()
                submit.assertIsDisplayed()
                checkNotNull(instrumentation.uiAutomation.takeScreenshot()).let { scrolled ->
                    File(directory, "${name}_last_field.png").outputStream().use {
                        scrolled.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                }

            }
            reports.put(report)
        }
        File(directory, "geometry.json").writeText(reports.toString(2))
    }
}
