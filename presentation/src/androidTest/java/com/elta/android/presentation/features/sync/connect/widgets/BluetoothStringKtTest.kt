package com.elta.android.presentation.features.sync.connect.widgets

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class BluetoothStringKtTest {

    @JvmField
    @Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.setContent { BluetoothString() }
    }

    @Test
    fun textContentTest() {
        composeTestRule.onNodeWithText("индикатор включенного Bluetooth", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun bluetoothImageViewTest() {
        composeTestRule.onNodeWithContentDescription("bluetooth icon")
            .assertIsDisplayed()
    }
}
