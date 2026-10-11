package com.elta.android

import android.content.Context
import android.os.SystemClock
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import androidx.test.filters.SdkSuppress
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.features.app.ui.AppActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@SdkSuppress(minSdkVersion = 30)
class AuthWindowInsetsTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(AppActivity::class.java)

    @Test
    fun loginButtonRemainsAboveSystemNavigationInTheActualAuthFlow() {
        activityRule.scenario.onActivity { it.router.startFlow(Screens.AuthFlow) }
        try {
            val deadline = SystemClock.uptimeMillis() + 10_000
            var ready = false
            while (!ready && SystemClock.uptimeMillis() < deadline) {
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                activityRule.scenario.onActivity {
                    val button = it.findViewById<View>(R.id.continueButtonView)
                    ready = button != null && button.isShown && button.height > 0
                }
                if (!ready) SystemClock.sleep(100)
            }
            assertTrue("Login screen did not become visible", ready)
            activityRule.scenario.onActivity { activity ->
                val input = activity.findViewById<View>(R.id.passwordInputView)
                input.requestFocus()
                (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }
            waitForKeyboard(visible = true)
            assertButtonIsSafe("keyboard visible")
            activityRule.scenario.onActivity { activity ->
                val input = activity.findViewById<View>(R.id.passwordInputView)
                (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(input.windowToken, 0)
            }
            waitForKeyboard(visible = false)
            assertButtonIsSafe("keyboard hidden")
            activityRule.scenario.recreate()
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            assertButtonIsSafe("activity recreated")
        } finally {
            activityRule.scenario.onActivity { it.router.exit() }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    private fun waitForKeyboard(visible: Boolean) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        var matches = false
        while (!matches && SystemClock.uptimeMillis() < deadline) {
            activityRule.scenario.onActivity {
                matches = it.window.decorView.rootWindowInsets?.isVisible(WindowInsets.Type.ime()) == visible
            }
            if (!matches) SystemClock.sleep(100)
        }
        assertTrue("Keyboard visibility did not become $visible", matches)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun assertButtonIsSafe(phase: String) {
        val deadline = SystemClock.uptimeMillis() + 3_000
        var safe = false
        var details = "Login button is missing"
        while (!safe && SystemClock.uptimeMillis() < deadline) {
            activityRule.scenario.onActivity { activity ->
                val button = activity.findViewById<View>(R.id.continueButtonView) ?: return@onActivity
                val navigationBottom = checkNotNull(activity.window.decorView.rootWindowInsets)
                    .getInsets(WindowInsets.Type.navigationBars() or WindowInsets.Type.ime()).bottom
                val position = IntArray(2)
                button.getLocationInWindow(position)
                val safeBottom = activity.window.decorView.height - navigationBottom
                safe = button.isShown && button.height > 0 && position[1] + button.height <= safeBottom
                details = "Login bottom=${position[1] + button.height}, safe bottom=$safeBottom, " +
                    "root padding=${(button.parent as View).paddingBottom}"
            }
            if (!safe) SystemClock.sleep(100)
        }
        assertTrue("$phase: $details", safe)
    }
}
