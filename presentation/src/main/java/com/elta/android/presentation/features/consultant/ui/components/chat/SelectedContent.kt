package com.elta.android.presentation.features.consultant.ui.components.chat

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.HSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerHalfMedium
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.ContextMenuUiEntity
import com.elta.android.presentation.features.consultant.model.MessageType
import com.elta.android.presentation.features.consultant.model.MessageUiEntity
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun SelectedContent(
    contentMenuEntity: ContextMenuUiEntity,
    connectState: ConnectState,
    onCopyClick: (MessageUiEntity) -> Unit,
    onEditClick: (MessageUiEntity) -> Unit,
    onDeleteClick: (MessageUiEntity) -> Unit
) {
    GetLocalProperties { dimens, _, _, _, _ ->
        Column(
            modifier = Modifier
                .padding(end = 12.dp, bottom = 12.dp)
                .requiredHeightIn(max = dimens.imageMessageSize.height),
            horizontalAlignment = Alignment.End,
        ) {
            MessageContextMenu(
                modifier = Modifier.padding(end = 12.dp),
                contentMenuEntity = contentMenuEntity,
                connectState = connectState,
                onCopyClick = onCopyClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick
            )
            VSpacerHalfMedium()
            contentMenuEntity.selectedMessage?.let { Message(message = it) }
        }
    }
}

@Composable
private fun MessageContextMenu(
    modifier: Modifier = Modifier,
    contentMenuEntity: ContextMenuUiEntity,
    connectState: ConnectState,
    onCopyClick: (MessageUiEntity) -> Unit = {},
    onEditClick: (MessageUiEntity) -> Unit = {},
    onDeleteClick: (MessageUiEntity) -> Unit = {}
) {
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
            modifier = modifier
                .wrapContentWidth()
                .background(
                    color = colors.white,
                    shape = shapes.textField
                )
                .padding(horizontal = dimens.smallDim)
                .clip(shapes.textField)
        ) {
            contentMenuEntity.selectedMessage?.let { message ->
                val isTextMessage = message.type == MessageType.Text

                if (isTextMessage) {
                    OptionText(
                        message = message,
                        iconId = R.drawable.ic_copy_message,
                        textId = R.string.consultant_copy_message,
                        textColor = colors.blackBlue,
                        onOptionClick = onCopyClick
                    )
                }
                if (connectState == ConnectState.Connect) {
                    if (isTextMessage) {
                        OptionText(
                            message = message,
                            iconId = R.drawable.ic_edit_message,
                            textId = R.string.consultant_edit_message,
                            textColor = colors.blackBlue,
                            onOptionClick = onEditClick
                        )
                    }
                    OptionText(
                        message = message,
                        iconId = R.drawable.ic_delete_message,
                        textId = R.string.consultant_delete_message,
                        textColor = colors.gOrangeB,
                        onOptionClick = onDeleteClick
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionText(
    message: MessageUiEntity,
    @DrawableRes iconId: Int,
    @StringRes textId: Int,
    textColor: Color,
    onOptionClick: (MessageUiEntity) -> Unit
) {
    GetLocalProperties { dimens, _, _, _, styles ->
        Row(
            modifier = Modifier
                .clickable {
                    onOptionClick(message)
                }
                .padding(dimens.consultantTopBarContentPadding)
        ) {
            Image(
                painter = painterResource(id = iconId),
                contentDescription = null,
                modifier = Modifier.padding(dimens.verySmallDim)
            )
            HSpacer(12.dp)
            Text(
                text = stringResource(id = textId),
                color = textColor,
                style = styles.infoDialog
            )
        }
    }
}

@Preview
@Composable
private fun PreviewMessageContextMenu() {
    MessageContextMenu(
        contentMenuEntity = ContextMenuUiEntity(
            isOpenContextMenu = false,
            selectedMessage = null
        ),
        connectState = ConnectState.Connect,
    )
}
