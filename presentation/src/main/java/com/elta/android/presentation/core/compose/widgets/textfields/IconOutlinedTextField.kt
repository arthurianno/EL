package com.elta.android.presentation.core.compose.widgets.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.core.compose.clearFocusOnKeyboardDismiss
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.components.CustomOutlinedTextField
import com.elta.android.presentation.theme.GetLocalProperties

sealed class IconOutlinedTextFieldAction : Action

object FocusChanged : IconOutlinedTextFieldAction()

@Immutable
data class IconOutlinedTextFieldWidgetState(
    val textField: TextFieldValue,
    val oldText: String,
    val isError: Boolean,
    val isEnabled: Boolean,
    val isFocused: Boolean
)

class IconOutlinedTextFieldWidgetModel : BaseWidgetModel<IconOutlinedTextFieldWidgetState>() {
    override fun createInitState(): IconOutlinedTextFieldWidgetState =
        IconOutlinedTextFieldWidgetState(
            textField = TextFieldValue(""),
            oldText = "",
            isError = false,
            isEnabled = true,
            isFocused = false
        )

    var textFilter: (TextFieldValue) -> TextFieldValue? = { it }


    fun setText(fieldValue: TextFieldValue) {
        textFilter(fieldValue)?.let {
            setState { state.value.copy(textField = it) }
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

    fun changeCursorPosition(textField: TextFieldValue) {
        setState {
            state.value.copy(
                textField = state.value.textField.copy(
                    selection = textField.selection
                )
            )
        }
    }

    fun focusChanged(isFocused: Boolean) {
        if (isFocused) {
            setState {
                state.value.copy(
                    textField = TextFieldValue(""),
                    oldText = state.value.textField.text,
                    isFocused = true
                )
            }
        } else {
            if (state.value.textField.text.isEmpty()) {
                setState {
                    state.value.copy(
                        textField = state.value.textField.copy(
                            text = state.value.oldText,
                            selection = TextRange(state.value.oldText.length)
                        ),
                        isFocused = false
                    )
                }
            }
        }
        sendAction(FocusChanged)
    }

    fun setFilter(filter: (TextFieldValue) -> TextFieldValue?) {
        textFilter = filter
    }

    fun setError(errorState: Boolean) {
        setState { state.value.copy(isError = errorState) }
    }

    fun setEnabled(isEnabled: Boolean) {
        setState { state.value.copy(isEnabled = isEnabled) }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RowScope.IconOutlinedTextField(
    widgetModel: IconOutlinedTextFieldWidgetModel,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(),
    keyboardType: KeyboardType = KeyboardType.Decimal,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    imeAction: ImeAction = ImeAction.Default,
    focusRequester: FocusRequester = FocusRequester(),
    focusManager: FocusManager = LocalFocusManager.current,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    fieldColors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
    isFocusRequested: Boolean = false,
    hint: String = "",
) {
    val state = widgetModel.state.collectAsState()
    val localMaterialShapes = Shapes(
        large = RoundedCornerShape(0.dp),
        medium = RoundedCornerShape(0.dp),
        small = RoundedCornerShape(0.dp)
    )
    val interactionSource = remember { MutableInteractionSource() }

    MaterialTheme(
        shapes = localMaterialShapes
    ) {
        LaunchedEffect(key1 = isFocusRequested) {
            if (isFocusRequested) focusRequester.requestFocus()
        }
        GetLocalProperties { dimens, _, colors, shapes, _ ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                CustomOutlinedTextField(
                    value = state.value.textField,
                    onValueChange = {
                        if (state.value.textField.text != it.text) {
                            widgetModel.setText(it)
                        } else {
                            widgetModel.changeCursorPosition(it)
                        }
                    },
                    enabled = state.value.isEnabled,
                    colors = fieldColors,
                    singleLine = true,
                    shape = shapes.textField,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    ),
                    modifier = modifier
                        .padding(paddingValues)
                        .fillMaxWidth()
                        .clickable {
                            focusManager.clearFocus()
                        }
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            widgetModel.focusChanged(it.isFocused)
                        }
                        .clearFocusOnKeyboardDismiss(),
                    textStyle = textStyle,
                    label = null,
                    placeholder = { Text(text = hint, color = colors.shadeBlack2) },
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    visualTransformation = VisualTransformation.None,
                    isError = state.value.isError,
                    maxLines = 1,
                    interactionSource = interactionSource
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
private fun PreviewIconOutlinedTextField() {
    Row(Modifier.background(Color.White)) {
        IconOutlinedTextField(
            widgetModel = IconOutlinedTextFieldWidgetModel().apply {
                setText(text = "12324567789")
            }
        )
    }
}
