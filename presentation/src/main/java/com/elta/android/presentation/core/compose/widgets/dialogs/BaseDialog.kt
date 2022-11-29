package com.elta.android.presentation.core.compose.widgets.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.theme.GetLocalProperties

data class BaseDialogWidgetState<D>(
    val title: String?,
    val message: String,
    val data: D?,
    val positiveButtonText: String?,
    val negativeButtonText: String?,
    val isOpen: Boolean,
    val isOverSizeClose: Boolean
)

class BaseDialogWidgetModel<D>(
    private val positiveOnCLick: (data: D?) -> Unit = {},
    private val negativeOnCLick: (data: D?) -> Unit = {}
) : BaseWidgetModel<BaseDialogWidgetState<D>>() {
    override fun createInitState(): BaseDialogWidgetState<D> =
        BaseDialogWidgetState(
            title = null,
            message = "",
            data = null,
            positiveButtonText = null,
            negativeButtonText = null,
            isOpen = false,
            isOverSizeClose = false
        )

    fun positiveClick() {
        dialogClose()
        positiveOnCLick(state.value.data)
    }

    fun negativeClick() {
        dialogClose()
        negativeOnCLick(state.value.data)
    }

    fun dialogClose() {
        setState { state.value.copy(isOpen = false) }
    }

    fun dialogOpen(data: D? = null) {
        setState { state.value.copy(isOpen = true, data = data) }
    }

    fun initDialog(
        title: String? = null,
        message: String,
        positiveButtonText: String? = null,
        negativeButtonText: String? = null,
        isOverSizeClose: Boolean = false
    ) {
        setState {
            state.value.copy(
                title = title,
                message = message,
                positiveButtonText = positiveButtonText,
                negativeButtonText = negativeButtonText,
                isOverSizeClose = isOverSizeClose
            )
        }
    }
}

@Composable
fun BaseDialog(widgetModel: BaseDialogWidgetModel<*>) {
    val state = widgetModel.state.collectAsState()
    GetLocalProperties { dimens, _, colors, shapes, types ->
        if (state.value.isOpen) {
            Dialog(
                onDismissRequest = { widgetModel.dialogClose() },
                properties = DialogProperties(
                    dismissOnClickOutside = state.value.isOverSizeClose
                )
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            color = colors.white,
                            shape = shapes.dialog
                        )
                        .padding(dimens.dialogPaddings)
                ) {
                    state.value.title?.let {
                        Text(text = it, style = types.title1)
                    }
                    VSpacer(height = dimens.halfMediumDim)
                    Text(
                        text = state.value.message,
                        style = types.body2
                    )
                    VSpacerSmall()
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DialogButton(state.value.negativeButtonText, widgetModel::negativeClick)
                        HSpacerSmall()
                        DialogButton(state.value.positiveButtonText, widgetModel::positiveClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogButton(
    text: String?,
    onClick: () -> Unit
) {
    GetLocalProperties { _, _, _, _, types ->
        text?.let {
            TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
                Text(text = it.uppercase(), style = types.dialogButton)
            }
        }
    }
}

@Preview
@Composable
fun PreviewDialog() {
    val widgetModel = BaseDialogWidgetModel<Nothing>().apply {
        initDialog(
            title = "Внимание",
            message = "Значение ХЕ превышает 99,9. Проверьте и измените добавленные продукты/блюда.",
            positiveButtonText = "ok"
        )
    }
    widgetModel.dialogOpen()
    BaseDialog(widgetModel = widgetModel)
}
