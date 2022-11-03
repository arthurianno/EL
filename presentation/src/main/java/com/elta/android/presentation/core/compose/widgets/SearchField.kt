package com.elta.android.presentation.core.compose.widgets

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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.theme.GetLocalProperties

data class SearchFocusChange(val focusState: FocusState) : Action

data class SearchFieldState(
    val hint: String,
    val text: String,
    @DrawableRes val icon: Int?,
    val isFocused: Boolean
)

class SearchFieldWidgetModel : BaseWidgetModel<SearchFieldState>() {
    fun setHint(hint: String?) {
        setState { state.value.copy(hint = hint.orEmpty()) }
    }

    fun setText(text: String?) {
        setState { state.value.copy(text = text.orEmpty()) }
    }

    fun setIcon(@DrawableRes icon: Int?) {
        setState { state.value.copy(icon = icon) }
    }

    fun setIsFocused(focusState: Boolean) {
        if (!focusState) setText(null)
        setState { state.value.copy(isFocused = focusState) }
    }

    override fun createInitState(): SearchFieldState =
        SearchFieldState(
            text = "",
            hint = "",
            isFocused = false,
            icon = R.drawable.ic_search
        )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchField(widgetModel: SearchFieldWidgetModel, searchInFocus: Boolean) {
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
                value = state.value.text,
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
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        widgetModel.setIsFocused(it.isFocused)
                        widgetModel.sendAction(SearchFocusChange(it))
                    }
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
