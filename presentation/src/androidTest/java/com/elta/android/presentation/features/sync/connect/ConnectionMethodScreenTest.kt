package com.elta.android.presentation.features.sync.connect

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.elta.android.presentation.R
import com.elta.android.presentation.theme.EltaTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ConnectionMethodScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun bothMethodsRemainVisibleAndRunTheirOwnActions() {
        var dmc = 0
        var pin = 0
        compose.setContent {
            EltaTheme { ConnectionMethodScreen(false, {}, {}, { dmc++ }, { pin++ }, {}) }
        }
        compose.onNodeWithText(compose.activity.getString(R.string.sync_connect_type_button_scan_dmc))
            .assertIsDisplayed().performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.sync_connect_type_button_enter_pin))
            .assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(1, dmc)
            assertEquals(1, pin)
        }
    }

    @Test
    fun connectLaterIsAvailableOnlyDuringOnboarding() {
        val onboarding = mutableStateOf(false)
        var later = 0
        compose.setContent {
            EltaTheme { ConnectionMethodScreen(onboarding.value, {}, {}, {}, {}, { later++ }) }
        }
        val button = compose.onNodeWithText(compose.activity.getString(R.string.sync_connect_later))
        button.assertDoesNotExist()
        compose.runOnIdle { onboarding.value = true }
        button.assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, later) }
    }

    @Test
    fun renderOnboardingAndProfileForReview() {
        val onboarding = mutableStateOf(false)
        compose.setContent {
            EltaTheme { ConnectionMethodScreen(onboarding.value, {}, {}, {}, {}, {}) }
        }
        val directory = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "method-screens")
        directory.mkdirs()
        listOf(false, true).forEach { value ->
            compose.runOnIdle { onboarding.value = value }
            compose.waitForIdle()
            val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
            File(directory, if (value) "onboarding.png" else "profile.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }
}
