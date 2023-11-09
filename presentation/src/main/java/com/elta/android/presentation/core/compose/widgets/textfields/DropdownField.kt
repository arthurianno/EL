package com.elta.android.presentation.core.compose.widgets.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.theme.GetLocalProperties

@Immutable
data class DropdownFieldWidgetState(
    val text: String,
    val oldText: String,
    val dropDownList: List<String>?,
    val isError: Boolean,
    val enabled: Boolean,
    val isExpanded: Boolean,
)

class DropdownFieldWidgetModel : BaseWidgetModel<DropdownFieldWidgetState>() {
    override fun createInitState(): DropdownFieldWidgetState = DropdownFieldWidgetState(
        text = "",
        oldText = "",
        dropDownList = null,
        isError = false,
        enabled = true,
        isExpanded = false
    )

    fun switchExpanded() {
        if (state.value.enabled) {
            setState { state.value.copy(isExpanded = !state.value.isExpanded) }
        }
    }

    fun setExpandedState(expandedState: Boolean) {
        setState { state.value.copy(isExpanded = expandedState) }
    }

    fun setText(text: String?) {
        setState { state.value.copy(text = text.orEmpty()) }
    }

    fun focusChanged(isFocused: Boolean) {
        if (isFocused) {
            setState {
                state.value.copy(
                    text = "", oldText = state.value.text
                )
            }
        } else {
            if (state.value.text.isEmpty()) {
                setState { state.value.copy(text = state.value.oldText) }
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        setState { state.value.copy(enabled = enabled) }
    }

    fun setDropDownList(list: List<String>?) {
        setState {
            state.value.copy(
                text = list?.first().orEmpty(),
                dropDownList = list,
                enabled = if (state.value.enabled) (list?.count() ?: 0) > 1 else state.value.enabled
            )
        }
    }

    fun setError(errorState: Boolean) {
        setState { state.value.copy(isError = errorState) }
    }
}

@Composable
fun RowScope.DropdownField(
    widgetModel: DropdownFieldWidgetModel,
    focusRequester: FocusRequester,
    focusManager: FocusManager
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
        GetLocalProperties { dimens, _, colors, shapes, types ->

            val borderColor = if (state.value.isError && state.value.enabled)
                colors.gOrangeB
            else
                colors.shadeBlack3

            Row(
                modifier = Modifier
                    .border(dimens.borderWidth, borderColor, shapes.textField)
                    .background(colors.paleGray, shape = shapes.textField)
                    .clickableWithNoRipple {
                        focusManager.clearFocus()
                        widgetModel.switchExpanded()
                    }
                    .focusRequester(focusRequester)
                    .padding(dimens.halfMediumDim)
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.value.text,
                    modifier = Modifier
                        .weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (state.value.enabled) {
                    Icon(
                        painterResource(id = R.drawable.ic_arrow_down),
                        contentDescription = null
                    )
                }
            }

            Box {
                CreateDropdownMenu(state, widgetModel)
            }

        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun BoxScope.CreateDropdownMenu(
    state: State<DropdownFieldWidgetState>,
    widgetModel: DropdownFieldWidgetModel,
) {
    val configuration = LocalConfiguration.current
    val width = configuration.screenWidthDp.dp - 32.dp
    GetLocalProperties { dimens, _, _, _, _ ->
        DropdownMenu(
            expanded = state.value.isExpanded,
            onDismissRequest = { widgetModel.setExpandedState(false) },
            properties = PopupProperties(
                usePlatformDefaultWidth = false
            ),
            modifier = Modifier
                .width(width)
                .align(Alignment.Center),
            offset = DpOffset(x = 0.dp, y = 64.dp)
        ) {
            state.value.dropDownList?.forEach {
                DropdownMenuItem(onClick = {
                    with(widgetModel) {
                        setText(it)
                        setExpandedState(false)
                    }
                }) {
                    Text(text = it)
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDropdownField() {
    val widget = DropdownFieldWidgetModel()

    Row {
        DropdownField(widget, FocusRequester(), LocalFocusManager.current)
    }
}

