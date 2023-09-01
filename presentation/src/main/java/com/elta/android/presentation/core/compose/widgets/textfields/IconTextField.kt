package com.elta.android.presentation.core.compose.widgets.textfields

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.theme.GetLocalProperties
import com.nullgr.core.collections.isNotNullOrEmpty

@Immutable
data class IconTextFieldWidgetState(
    val text: String,
    val oldText: String,
    @DrawableRes val leadIcon: Int?,
    val dropDownList: List<String>?,
    val isError: Boolean,
    val isDropDown: Boolean,
    val isExpanded: Boolean
)

class IconTextFieldWidgetModel : BaseWidgetModel<IconTextFieldWidgetState>() {
    override fun createInitState(): IconTextFieldWidgetState =
        IconTextFieldWidgetState(
            text = "",
            oldText = "",
            leadIcon = null,
            dropDownList = null,
            isError = false,
            isDropDown = false,
            isExpanded = false
        )

    var textFilter: (String) -> String? = { it }

    fun switchExpanded() {
        setState { state.value.copy(isExpanded = !state.value.isExpanded) }
    }

    fun setExpandedState(expandedState: Boolean) {
        setState { state.value.copy(isExpanded = expandedState) }
    }

    fun setText(text: String?) {
        textFilter(text.orEmpty())?.let {
            setState { state.value.copy(text = it) }
        }
    }

    fun focusChanged(isFocused: Boolean) {
        if (isFocused) {
            setState {
                state.value.copy(
                    text = "",
                    oldText = state.value.text
                )
            }
        } else {
            if (state.value.text.isEmpty()) {
                setState { state.value.copy(text = state.value.oldText) }
            }
        }
    }

    fun setIcon(@DrawableRes icon: Int?) {
        setState { state.value.copy(leadIcon = icon) }
    }

    fun setDropDownList(list: List<String>?) {
        setState {
            state.value.copy(
                text = list?.first().orEmpty(),
                dropDownList = list,
                isDropDown = list.isNotNullOrEmpty()
            )
        }
    }

    fun setError(errorState: Boolean) {
        setState { state.value.copy(isError = errorState) }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun IconTextField(
    widgetModel: IconTextFieldWidgetModel,
    paddingValues: PaddingValues = PaddingValues(),
    keyboardType: KeyboardType = KeyboardType.Decimal,
    imeAction: ImeAction = ImeAction.Go,
    focusRequester: FocusRequester = FocusRequester(),
    hint: String = ""
) {
    val state = widgetModel.state.collectAsState()
    val isDropDown = state.value.isDropDown
    val localMaterialShapes = Shapes(
        large = RoundedCornerShape(0.dp),
        medium = RoundedCornerShape(0.dp),
        small = RoundedCornerShape(0.dp)
    )
    val configuration = LocalConfiguration.current
    val keyboardController = LocalSoftwareKeyboardController.current
    MaterialTheme(
        shapes = localMaterialShapes
    ) {
        GetLocalProperties { dimens, _, colors, _, _ ->
            Column {
                TextField(
                    value = state.value.text,
                    onValueChange = widgetModel::setText,
                    isError = state.value.isError,
                    leadingIcon = state.value.leadIcon?.let {
                        {
                            Icon(
                                painter = painterResource(id = it),
                                contentDescription = null
                            )
                        }
                    },
                    trailingIcon = state.value.dropDownList?.let {
                        {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_down),
                                contentDescription = null,
                            )
                        }
                    },
                    enabled = !isDropDown,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                        .clickable { widgetModel.switchExpanded() }
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            if (!isDropDown) {
                                widgetModel.focusChanged(it.isFocused)
                            }
                        },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = colors.blackBlue,
                        disabledTextColor = colors.blackBlue,
                        backgroundColor = colors.white,
                        unfocusedIndicatorColor = colors.shadeBlack3,
                        focusedIndicatorColor = colors.shadeBlack0,
                        errorIndicatorColor = colors.gOrangeB,
                        cursorColor = colors.blackBlue
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction
                    ),
                    keyboardActions = KeyboardActions(
                        onAny = { keyboardController?.hide() }
                    ),
                    singleLine = true,
                    readOnly = isDropDown,
                    placeholder = {
                        Text(
                            text = hint,
                            color = colors.shadeBlack2
                        )
                    }
                )
                if (isDropDown) {
                    DropdownMenu(
                        expanded = state.value.isExpanded,
                        onDismissRequest = { widgetModel.setExpandedState(false) },
                        properties = PopupProperties(
                            usePlatformDefaultWidth = false
                        ),
                        modifier = Modifier
                            .width(configuration.screenWidthDp.dp - dimens.contentPadding * 2),
                        offset = dimens.textFieldPopupOffset
                    ) {
                        state.value.dropDownList?.forEach {
                            DropdownMenuItem(
                                onClick = {
                                    with(widgetModel) {
                                        setText(it)
                                        setExpandedState(false)
                                    }
                                }
                            ) {
                                Text(text = it)
                            }
                        }
                    }
                }
            }
        }
    }
}
