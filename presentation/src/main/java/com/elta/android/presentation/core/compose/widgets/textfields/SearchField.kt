package com.elta.android.presentation.core.compose.widgets.textfields

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.widgets.animation.HorizontallyAnimation
import com.elta.android.presentation.core.compose.widgets.buttons.ButtonCircle
import com.elta.android.presentation.theme.GetLocalProperties

sealed class SearchFiledAction : Action {
    data class FocusChanged(val focusState: FocusState) : SearchFiledAction()
}

@Immutable
data class SearchFieldWidgetState(
    val hint: String,
    val textField: TextFieldValue,
    @DrawableRes val icon: Int?,
    val isFocused: Boolean
)

class SearchFieldWidgetModel : BaseWidgetModel<SearchFieldWidgetState>() {
    fun setHint(hint: String?) {
        setState { state.value.copy(hint = hint.orEmpty()) }
    }

    fun setText(fieldValue: TextFieldValue) {
        setState { state.value.copy(textField = fieldValue) }
    }

    fun setTextAndCursorToEnd(text: String?) {
        setState {
            state.value.copy(
                textField = TextFieldValue(
                    text = text.orEmpty(),
                    selection = TextRange(text.orEmpty().length)
                )
            )
        }
    }

    fun focusChanged(focusState: FocusState) {
        setState { state.value.copy(isFocused = focusState.isFocused) }
        sendAction(SearchFiledAction.FocusChanged(focusState))
    }

    fun clear() {
        setTextAndCursorToEnd(null)
    }

    override fun createInitState(): SearchFieldWidgetState =
        SearchFieldWidgetState(
            textField = TextFieldValue(""),
            hint = "",
            isFocused = false,
            icon = R.drawable.ic_search
        )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchField(
    widgetModel: SearchFieldWidgetModel,
    searchInFocus: Boolean
) {
    val state = widgetModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    GetLocalProperties { _, _, colors, shapes, _ ->
        Row(modifier = Modifier.fillMaxWidth()) {
            HorizontallyAnimation(visualState = searchInFocus) {
                IconButton(onClick = { focusManager.clearFocus(force = true) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        tint = colors.blackBlue,
                        contentDescription = null
                    )
                }
            }
            TextField(
                value = state.value.textField,
                onValueChange = widgetModel::setText,
                singleLine = true,
                shape = shapes.textField,
                placeholder = {
                    Text(
                        text = state.value.hint,
                        color = colors.shadeBlack2
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = colors.blackBlue,
                    backgroundColor = colors.paleGray
                ),
                leadingIcon = if (searchInFocus) {
                    null
                } else {
                    state.value.icon?.let<Int, @Composable () -> Unit> { icon ->
                        searchIcon(icon, colors.shadeBlack2)
                    }
                },
                trailingIcon = {
                    if (state.value.textField.text.isNotEmpty()){
                        HorizontallyAnimation(visualState = searchInFocus, toLeft = false) {
                            ButtonCircle(
                                icon = R.drawable.ic_search_clean,
                                onClick = widgetModel::clear,
                                contentDescriptionId = R.string.content_description_close_button
                            )
                        }
                    }
                },
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged(widgetModel::focusChanged)
            )
        }
    }
}

private fun searchIcon(@DrawableRes icon: Int, color: Color): @Composable (() -> Unit) = {
    Icon(
        painter = painterResource(id = icon),
        tint = color,
        contentDescription = null
    )
}
