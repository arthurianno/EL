package com.elta.android.presentation.core.compose.widgets.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.widgets.VerticallyAnimation
import com.elta.android.presentation.theme.GetLocalProperties

object DownButtonClick : Action

data class DownButtonState(
    val text: String,
    val enable: Boolean,
    val visible: Boolean
)

class DownButtonWidgetModel : BaseWidgetModel<DownButtonState>() {

    fun setText(text: String) {
        setState { state.value.copy(text = text) }
    }

    fun enable() {
        setState { state.value.copy(enable = true) }
    }

    fun disable() {
        setState { state.value.copy(enable = false) }
    }

    fun onClick() {
        sendAction(DownButtonClick)
    }

    infix fun visibilityState(visibilityState: Boolean) {
        setState { state.value.copy(visible = visibilityState) }
    }

    override fun createInitState(): DownButtonState =
        DownButtonState(
            text = "",
            enable = true,
            visible = true
        )
}

@Composable
fun BoxScope.DownButton(widgetModel: DownButtonWidgetModel) {
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
                            onClick = widgetModel::onClick
                        )
                        .fillMaxWidth()
                        .height(dimens.downButtonHeight)
                        .then(backgroundModifier)
                ) {
                    Text(text = state.value.text, color = textColor)
                }
            }
        }
    }
}
