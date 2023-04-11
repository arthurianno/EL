package com.elta.android.presentation.features.sync.connect.widgets

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val TAG = "Device_Connect_Test"

internal class DeviceConnectAppTopBarKtTest {
    @JvmField
    @Rule
    val composeTestRule = createComposeRule()
    private val appTopBarWidgetModel = BaseAppTopBarWidgetModel()
    private val endTextButton = composeTestRule.onNodeWithText("Возникли сложности?")
    private val startButton = composeTestRule
        .onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))

    @Before
    fun start() {
        composeTestRule.setContent {
            AppTopBar(appTopBarModel = appTopBarWidgetModel)
        }
    }

    @Test
    fun createTest() {
        composeTestRule
            .onRoot()
            .assertExists()
    }

    @Test
    fun textButtonViewTest() {
        endTextButton
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun textButtonDimensionsTest() {
        endTextButton
            .assertWidthIsAtLeast(147.dp)
            .assertHeightIsAtLeast(20.dp)
    }

    @Test
    fun startButtonViewTest() {
        startButton
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun startButtonDimensionsTest() {
        startButton
            .assertWidthIsAtLeast(24.dp)
            .assertHeightIsAtLeast(24.dp)
    }
}
