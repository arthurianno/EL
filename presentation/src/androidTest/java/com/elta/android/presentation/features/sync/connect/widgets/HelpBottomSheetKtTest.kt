package com.elta.android.presentation.features.sync.connect.widgets

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.core.compose.tests.TestTags
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class HelpBottomSheetKtTest {

    @JvmField
    @Rule
    val composeTestRule = createComposeRule()
    private val downButton = DownButtonWidgetModel()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            HelpBottomSheet(
                downButtonModel = downButton,
                closeOnClick = {}
            )
        }
    }

    @Test
    fun helpBottomSheetViewTest() {
        with(composeTestRule) {
            onNodeWithText("Не получается подключить устройство через DMC?")
                .assertIsDisplayed()
            onNodeWithText("Если по каким-либо причинам Вам не удаётся подключиться с помощью считывания DMC-кода, то советуем воспользоваться подключением через ПИН-код.")
                .assertIsDisplayed()
            onNodeWithContentDescription("кнопка Закрыть")
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithTag(TestTags.DownButton.name)
                .assertIsDisplayed()
                .assertHasClickAction()
                .assertHeightIsEqualTo(52.dp)
        }
    }
}
