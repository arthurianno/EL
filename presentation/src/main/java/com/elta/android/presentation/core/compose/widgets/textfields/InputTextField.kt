package com.elta.android.presentation.core.compose.widgets.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.widgets.VSpacerHalfMedium
import com.elta.android.presentation.features.calcutator.custom.component.bottomBorder
import com.elta.android.presentation.theme.GetLocalProperties

@Immutable
data class InputTextFieldWidgetState(
    val hint: String,
    val header: String,
    val description: String,
    val isError: Boolean,
    val textField: TextFieldValue,
    val maxLength: Int,
    val enabled: Boolean,
    val isFocused: Boolean
)

class InputTextFieldWidgetModel : BaseWidgetModel<InputTextFieldWidgetState>() {
    override fun createInitState(): InputTextFieldWidgetState = InputTextFieldWidgetState(
        hint = "",
        header = "",
        description = "",
        textField = TextFieldValue(""),
        enabled = true,
        isError = false,
        isFocused = false,
        maxLength = 100,
    )

    var textFilter: (TextFieldValue) -> TextFieldValue? = { it }

    fun setHint(hint: String?) {
        setState { state.value.copy(hint = hint.orEmpty()) }
    }

    fun setHeader(header: String?) {
        setState { state.value.copy(header = header.orEmpty()) }
    }


    fun setText(fieldValue: TextFieldValue) {
        if (fieldValue.text.length <= state.value.maxLength) {
            textFilter(fieldValue)?.let {
                setState { state.value.copy(textField = it) }
            }
        }
    }

    fun setText(text: String) {
        setText(
            state.value.textField.copy(
                text = text,
                selection = TextRange(text.length)
            )
        )
    }

    fun setError(errorState: Boolean) {
        setState {
            state.value.copy(
                isError = errorState
            )
        }
    }

    fun setMaxLength(maxLength: Int) {
        setState {
            state.value.copy(maxLength = maxLength)
        }
    }

    fun setEnabled(enableState: Boolean) {
        setState { state.value.copy(enabled = enableState) }
    }

    fun setDescription(description: String?) {
        setState { state.value.copy(description = description.orEmpty()) }
    }

    fun focusChanged(focusState: FocusState) {
        setState { state.value.copy(isFocused = focusState.isFocused) }
        sendAction(SearchFieldAction.FocusChanged(focusState))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalComposeUiApi
@Composable
fun InputText(
    widgetModel: InputTextFieldWidgetModel,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    focusRequester: FocusRequester = FocusRequester(),
    focusManager: FocusManager = LocalFocusManager.current,
    isFocusRequested: Boolean = false,
) {
    val state = widgetModel.state.collectAsState()

    val localMaterialShapes = Shapes(
        large = RoundedCornerShape(0.dp),
        medium = RoundedCornerShape(0.dp),
        small = RoundedCornerShape(0.dp)
    )

    MaterialTheme(
        shapes = localMaterialShapes
    ) {
        LaunchedEffect(key1 = isFocusRequested) {
            if (isFocusRequested)
                focusRequester.requestFocus()
        }

        GetLocalProperties { dimens, brash, colors, shapes, types ->

            val interactionSource = remember { MutableInteractionSource() }

            val keyboardController = LocalSoftwareKeyboardController.current

            var borderWidth by remember { mutableStateOf(dimens.borderWidth) }
            var borderColor by remember { mutableStateOf(colors.shadeBlack3) }

            borderWidth = if (state.value.isError || state.value.isFocused)
                dimens.borderWidthMedium
                else dimens.borderWidth

            borderColor = when {
                state.value.isError -> colors.red
                state.value.isFocused -> colors.shadeBlack0
                else -> colors.shadeBlack3
            }

            Column(
                modifier = Modifier.background(color = colors.white)
            ) {

                Text(
                    text = state.value.header,
                    style = types.subtitle1,
                    color = colors.shadeBlack2
                )

                VSpacerHalfMedium()

                BasicTextField(
                    value = state.value.textField,
                    onValueChange = widgetModel::setText,
                    textStyle = types.subtitle1,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = KeyboardActions(onAny = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }),
                    enabled = state.value.enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            widgetModel.focusChanged(focusState)
                        }
                        .bottomBorder(borderWidth, borderColor),

                    ) { innerTextField ->
                    TextFieldDefaults.OutlinedTextFieldDecorationBox(value = state.value.textField.text,
                        visualTransformation = VisualTransformation.None,
                        innerTextField = innerTextField,
                        singleLine = false,
                        enabled = true,
                        interactionSource = interactionSource,
                        contentPadding = PaddingValues(
                            horizontal = dimens.zero, vertical = dimens.halfMediumDim
                        ),
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = colors.blackBlue,
                            disabledTextColor = colors.blackBlue,
                            backgroundColor = colors.white,
                            unfocusedIndicatorColor = colors.shadeBlack3,
                            focusedIndicatorColor = colors.shadeBlack0,
                            errorIndicatorColor = colors.gOrangeB,
                            cursorColor = colors.blackBlue,
                            disabledIndicatorColor = if (state.value.isError) colors.gOrangeB else colors.shadeBlack3
                        ),
                        placeholder = {
                            Text(
                                text = state.value.hint,
                                style = types.subtitle1,
                                color = colors.shadeBlack2
                            )
                        },
                        border = { })
                }
                Text(
                    text = state.value.description,
                    style = if (state.value.isError) types.descriptionError else types.description,
                    modifier = Modifier
                        .padding(top = dimens.smallDim)
                        .fillMaxWidth()
                )
            }
        }

    }


}


@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
private fun PreviewInputText() {
    InputText(InputTextFieldWidgetModel())
}

