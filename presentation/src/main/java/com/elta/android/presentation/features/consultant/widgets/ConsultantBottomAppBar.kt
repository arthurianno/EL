package com.elta.android.presentation.features.consultant.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.RoundedButton
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.theme.GetLocalProperties

private const val MESSAGE_MAX_LINES = 10

internal data class ConsultantBottomAppBarWidgetState(
    val connectState: ConnectState,
    val messageText: TextFieldValue
)

internal class ConsultantBottomAppBarWidgetModel :
    BaseWidgetModel<ConsultantBottomAppBarWidgetState>() {
    override fun createInitState(): ConsultantBottomAppBarWidgetState =
        ConsultantBottomAppBarWidgetState(
            connectState = ConnectState.Offline,
            messageText = TextFieldValue()
        )

    fun setText(newText: TextFieldValue) {
        setState { state.value.copy(messageText = newText) }
    }
}

@Composable
internal fun ConsultantBottomAppBar(widgetModel: ConsultantBottomAppBarWidgetModel) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colors.shadeBlack4)
                .padding(dimens.consultantBottomBarContentPadding)
        ) {
            FileButton(widgetModel)
            MessageField(widgetModel)
            SendButton(widgetModel)
        }
    }
}

@Composable
private fun BoxScope.SendButton(widgetModel: ConsultantBottomAppBarWidgetModel) {
    GetLocalProperties { _, _, colors, _, _ ->
        Box(modifier = Modifier.Companion.align(Alignment.BottomEnd)) {
            RoundedButton(
                icon = R.drawable.ic_voice_message,
                background = colors.gGreenB,
                border = colors.gGreenB,
                onClick = { widgetModel.sendAction(ConsultantAction.VoiceClick) }
            )
        }
    }
}

@Composable
private fun BoxScope.FileButton(widgetModel: ConsultantBottomAppBarWidgetModel) {
    Box(modifier = Modifier.Companion.align(Alignment.BottomStart)) {
        RoundedButton(
            icon = R.drawable.ic_file,
            onClick = { widgetModel.sendAction(ConsultantAction.FileClick) }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BoxScope.MessageField(widgetModel: ConsultantBottomAppBarWidgetModel) {
    val state = widgetModel.state.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }
    val visualTransformation = remember { VisualTransformation.None }
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(dimens.messageTextFiledPadding)
        ) {
            BasicTextField(
                value = state.value.messageText,
                onValueChange = widgetModel::setText,
                interactionSource = interactionSource,
                maxLines = MESSAGE_MAX_LINES,
                visualTransformation = visualTransformation,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colors.white, shape = shapes.consultantTextField)
                    .border(
                        color = colors.shadeBlack3,
                        shape = shapes.consultantTextField,
                        width = dimens.borderWidth
                    )
            ) { innerTextField ->
                TextFieldDefaults.TextFieldDecorationBox(
                    value = state.value.messageText.text,
                    innerTextField = innerTextField,
                    enabled = true,
                    interactionSource = interactionSource,
                    visualTransformation = visualTransformation,
                    singleLine = false,
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = colors.blackBlue,
                        backgroundColor = colors.white,
                        cursorColor = colors.blackBlue,
                        errorIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        placeholderColor = colors.shadeBlack1
                    ),
                    placeholder = { Text(text = stringResource(id = R.string.consultant_message_placeholder)) },
                    contentPadding = dimens.messageTextPadding
                )
            }
        }
    }
}

@Preview
@Composable
fun BottomBarPreview() {
    val widgetModel = ConsultantBottomAppBarWidgetModel()
    ConsultantBottomAppBar(widgetModel = widgetModel)
}
