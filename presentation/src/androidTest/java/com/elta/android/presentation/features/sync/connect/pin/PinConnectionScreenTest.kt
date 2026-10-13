package com.elta.android.presentation.features.sync.connect.pin

import android.graphics.Bitmap
import android.os.SystemClock
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.elta.android.presentation.R
import com.elta.android.presentation.features.sync.connect.base.ui.adapter.items.DeviceItem
import com.elta.android.presentation.theme.EltaTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PinConnectionScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun configureWindow() {
        compose.runOnUiThread {
            compose.activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    @Test
    fun pinInputEnablesConfirmationOnlyAfterThreeDigits() {
        val state = mutableStateOf(PinConnectionUiState(stage = PinConnectionStage.ENTER_PIN))
        var confirmations = 0
        compose.setContent {
            EltaTheme {
                PinConnectionScreen(state.value, {}, {}, {}, { state.value = state.value.copy(pin = it) }, { confirmations++ })
            }
        }
        val button = compose.onNodeWithText(compose.activity.getString(R.string.sync_pin_confirm))
        val input = compose.onNodeWithContentDescription(compose.activity.getString(R.string.sync_pin_input_label))
        input.assertIsDisplayed().performClick()
        compose.waitUntil(5000) {
            var visible = false
            compose.runOnUiThread {
                visible = ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            }
            visible
        }
        compose.waitForIdle()
        SystemClock.sleep(350)
        val canvasBounds = compose.onNodeWithTag(PIN_ANIMATION_TAG).fetchSemanticsNode().boundsInRoot
        val displayBounds = compose.onNodeWithTag(PIN_DISPLAY_TAG).fetchSemanticsNode().boundsInRoot
        assertEquals(canvasBounds.top + canvasBounds.width * 0.373f, displayBounds.top, 1f)
        assertEquals(canvasBounds.width * 0.524f, displayBounds.width, 1f)
        input.performTextInput("03")
        button.assertIsNotEnabled()
        input.performTextInput("7")
        button.assertIsDisplayed().assertIsEnabled().performClick()
        compose.runOnIdle {
            assertEquals("037", state.value.pin)
            assertEquals(1, confirmations)
        }
        val directory = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "pin-screens")
        directory.mkdirs()
        compose.waitForIdle()
        val screenshot = checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        File(directory, "enter_pin_keyboard.png").outputStream().use { screenshot.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun selectedDeviceEnablesConnectionAndAllowsDeselection() {
        val devices = listOf(
            DeviceItem("one", "SatelliteOnlineAA59", "one", false, false),
            DeviceItem("two", "SatelliteOnlineAA62", "two", false, true)
        )
        val state = mutableStateOf(PinConnectionUiState(stage = PinConnectionStage.FOUND, devices = devices))
        compose.setContent {
            EltaTheme {
                PinConnectionScreen(state.value, {}, {}, { selected ->
                    val updated = state.value.devices.map { it.copy(isSelected = it.address == selected.address && !selected.isSelected) }
                    state.value = state.value.copy(devices = updated, canConnect = updated.any { it.isSelected })
                }, {}, {})
            }
        }
        val button = compose.onNodeWithText(compose.activity.getString(R.string.sync_pin_connect))
        button.assertIsNotEnabled()
        compose.onNodeWithText("SatelliteOnlineAA62").performClick()
        button.assertIsEnabled()
        compose.onNodeWithText("SatelliteOnlineAA62").performClick()
        button.assertIsNotEnabled()
    }

    @Test
    fun illustrationBoundsStayFixedWhenDevicesAppearAndListGrows() {
        val state = mutableStateOf(PinConnectionUiState(stage = PinConnectionStage.SEARCH))
        compose.setContent { EltaTheme { PinConnectionScreen(state.value, {}, {}, {}, {}, {}) } }
        compose.waitUntil(5000) {
            compose.onRoot().fetchSemanticsNode().boundsInRoot.height > compose.activity.resources.displayMetrics.heightPixels * 0.65f
        }
        val illustration = compose.onNodeWithTag(PIN_ANIMATION_TAG)
        val baseline = illustration.fetchSemanticsNode().boundsInRoot
        for (count in 1..8) {
            compose.runOnIdle {
                state.value = state.value.copy(
                    stage = PinConnectionStage.FOUND,
                    devices = (1..count).map {
                        DeviceItem(it, "SatelliteOnlineAA$it", "$it", false, it == count)
                    }
                )
            }
            compose.waitForIdle()
            assertEquals(baseline, illustration.fetchSemanticsNode().boundsInRoot)
        }
        listOf(PinConnectionStage.NOT_FOUND, PinConnectionStage.CONNECTING, PinConnectionStage.CONNECTED,
            PinConnectionStage.SYNC_COMPLETED, PinConnectionStage.CONNECT_ERROR, PinConnectionStage.SYNC_ERROR).forEach { stage ->
            compose.runOnIdle { state.value = state.value.copy(stage = stage) }
            compose.waitForIdle()
            assertEquals(baseline, illustration.fetchSemanticsNode().boundsInRoot)
        }
    }

    @Test
    fun renderEveryStageForVisualReview() {
        val state = mutableStateOf(PinConnectionUiState())
        compose.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                EltaTheme { PinConnectionScreen(state.value, {}, {}, {}, {}, {}) }
            }
        }
        val directory = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "pin-screens")
        directory.mkdirs()
        val devices = listOf(
            DeviceItem("one", "SatelliteOnlineAA59", "one", false, false),
            DeviceItem("two", "SatelliteOnlineAA62", "two", true, false),
            DeviceItem("three", "SatelliteOnlineAA63", "three", false, true)
        )
        val screens = PinConnectionStage.entries.map {
            it.name.lowercase() to PinConnectionUiState(stage = it, devices = devices, canConnect = true, pin = "376")
        } + listOf(
            "found_unselected" to PinConnectionUiState(stage = PinConnectionStage.FOUND, devices = devices.map { it.copy(isSelected = false) }),
            "enter_pin_empty" to PinConnectionUiState(stage = PinConnectionStage.ENTER_PIN),
            "enter_pin_incorrect" to PinConnectionUiState(stage = PinConnectionStage.ENTER_PIN, pinIncorrect = true)
        )
        screens.forEach { (name, screen) ->
            compose.runOnIdle { state.value = screen }
            compose.waitForIdle()
            SystemClock.sleep(if (screen.stage == PinConnectionStage.ENTER_PIN) 1100 else 500)
            compose.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
                File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
        }
    }
}
