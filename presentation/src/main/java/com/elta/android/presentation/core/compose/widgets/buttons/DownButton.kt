package com.elta.android.presentation.core.compose.widgets.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.tests.TestTags
import com.elta.android.presentation.core.compose.widgets.animation.VerticallyAnimation
import com.elta.android.presentation.theme.GetLocalProperties

object DownButtonClick : Action

@Immutable
data class DownButtonWidgetState(
    val text: String,
    val enable: Boolean,
    val isLoading: Boolean,
    val visible: Boolean,
    val ignoreClick: Boolean,
)

class DownButtonWidgetModel : BaseWidgetModel<DownButtonWidgetState>() {

    fun setText(text: String) {
        setState { state.value.copy(text = text) }
    }

    fun setEnableState(enableState: Boolean) {
        setState { state.value.copy(enable = enableState) }
    }

    fun setLoading(isLoading: Boolean) {
        setState { state.value.copy(isLoading = isLoading) }
    }

    infix fun visibilityState(visibilityState: Boolean) {
        setState { state.value.copy(visible = visibilityState) }
    }

    fun setIgnoreClick(ignoreClick: Boolean) {
        setState { state.value.copy(
            ignoreClick = ignoreClick
        ) }
    }

    override fun createInitState(): DownButtonWidgetState =
        DownButtonWidgetState(
            text = "",
            isLoading = false,
            enable = true,
            visible = true,
            ignoreClick = false,
        )
}

@Composable
fun DownButton(
    widgetModel: DownButtonWidgetModel,
    onClickAction: Action = DownButtonClick
) {
    Box {
        DownButton(widgetModel = widgetModel, onClickAction = onClickAction)
    }
}

@Composable
fun BoxScope.DownButton(
    widgetModel: DownButtonWidgetModel,
    onClickAction: Action = DownButtonClick
) {
    val state = widgetModel.state.collectAsState()
    GetLocalProperties { dimens, brash, colors, _, _ ->
        val isEnable = state.value.enable
        val textColor = if (isEnable) {
            colors.white
        } else {
            colors.shadeBlack1
        }
        val backgroundModifier = if (isEnable) {
            Modifier.background(brush = brash.downButton)
        } else {
            Modifier.background(color = colors.shadeBlack3)
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            VerticallyAnimation(
                visualState = state.value.visible,
                toUp = false
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clickable(
                            enabled = isEnable,
                            role = Role.Button,
                            onClick = {
                                if (!state.value.ignoreClick) {
                                    widgetModel.sendAction(onClickAction)
                                }
                            }
                        )
                        .fillMaxWidth()
                        .height(dimens.downButtonHeight)
                        .testTag(TestTags.DownButton.name)
                        .then(backgroundModifier)
                ) {
                    if (state.value.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxHeight()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .wrapContentHeight(Alignment.CenterVertically),
                            color = colors.white
                        )
                    } else {
                        Text(text = state.value.text, color = textColor)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDownButton() {
    val widgetModel = DownButtonWidgetModel()
    widgetModel.setLoading(true)
    DownButton(widgetModel = widgetModel)
}
