package com.elta.android.presentation.core.compose.widgets.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.theme.GetLocalProperties

data class InfoDialogWidgetState<D>(
    val message: String,
    val data: D?,
    val buttonText: String?,
    val isOpen: Boolean,
    val isOverSizeClose: Boolean
)

class InfoDialogWidgetModel<D>(
    private val onCLick: (data: D?) -> Unit = {}
) : BaseWidgetModel<InfoDialogWidgetState<D>>() {
    override fun createInitState(): InfoDialogWidgetState<D> = InfoDialogWidgetState(
        message = "",
        data = null,
        buttonText = "",
        isOpen = false,
        isOverSizeClose = false
    )


    fun infoClick() {
        dialogClose()
        onCLick(state.value.data)
    }

    fun dialogClose() {
        setState { state.value.copy(isOpen = false) }
    }

    fun dialogOpen(data: D? = null, message: String? = null) {
        setState {
            state.value.copy(
                isOpen = true, data = data, message = message ?: state.value.message
            )
        }
    }

    fun initDialog(
        message: String, buttonText: String? = null, isOverSizeClose: Boolean = false
    ) {
        setState {
            state.value.copy(
                message = message, buttonText = buttonText, isOverSizeClose = isOverSizeClose
            )
        }
    }
}

@Composable
fun InfoDialog(widgetModel: InfoDialogWidgetModel<*>) {
    val state = widgetModel.state.collectAsState()
    if (state.value.isOpen) {

        Dialog(
            onDismissRequest = { widgetModel.dialogClose() }, properties = DialogProperties(
                dismissOnClickOutside = state.value.isOverSizeClose
            )
        ) {
            DialogContent(
                message = state.value.message,
                buttonText = state.value.buttonText.orEmpty()
            ) {
                widgetModel.infoClick()
            }
        }
    }
}

@Composable
private fun DialogContent(
    message: String,
    buttonText: String,
    infoClick: () -> Unit,
) {
    GetLocalProperties { dimens, _, colors, shapes, types ->
        Column(
            modifier = Modifier
                .background(color = colors.white, shape = shapes.infoDialog),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VSpacer(height = dimens.halfMediumDim)
            Text(
                text = message,
                style = types.infoDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            VSpacerSmall()

            Box(modifier = Modifier
                .clickable { infoClick.invoke() }
                .background(color = colors.gGreenB, shape = shapes.infoDialogBottom)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonText,
                    style = types.infoDialogButton,
                    color = colors.white
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDialogContent() {
    DialogContent(
        message = "Курица и индейка A La King с овощами в сливках, белый или суповой соус (включает морковку, брокколи и/или салат, без картофеля) на основе куриного бульона",
        buttonText = "Закрыть"
    ) {

    }
}
