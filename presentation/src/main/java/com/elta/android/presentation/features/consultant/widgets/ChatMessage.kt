package com.elta.android.presentation.features.consultant.widgets

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.elta.android.domain.features.consultant.model.WebimContentType
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.domain.features.consultant.model.WebimOwner
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.HSpacerVerySmall
import com.elta.android.presentation.features.consultant.model.ChatUiEntity
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.theme.GetLocalProperties

internal data class ChatMessageWidgetState(
    val message: ChatUiEntity,
    val attachmentCacheUri: Uri?
)

internal class ChatMessageWidgetModel : BaseWidgetModel<ChatMessageWidgetState>() {
    override fun createInitState(): ChatMessageWidgetState =
        ChatMessageWidgetState(
            message = ChatUiEntity(
                owner = WebimOwner.User,
                type = null,
                thumbnail = null,
                attachmentUrl = null,
                fileSize = null,
                text = "",
                date = "",
                sendStatus = WebimMessageSendStatus.Sending,
                isRead = false
            ),
            attachmentCacheUri = null
        )

    fun setMessage(message: ChatUiEntity) {
        setState { state.value.copy(message = message) }
    }
}

@Composable
internal fun ChatMessage(widgetModel: ChatMessageWidgetModel) {
    val state = widgetModel.state.collectAsState()
    val message = state.value.message
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        val backgroundColor = when (state.value.message.owner) {
            WebimOwner.Operator -> colors.white
            WebimOwner.User -> when (state.value.message.type) {
                WebimContentType.Voice -> colors.gGreenB10
                else -> colors.shadeBlack4
            }
        }
        Box(
            modifier = Modifier
                .clip(shape = shapes.chatMessage)
                .border(
                    shape = shapes.chatMessage,
                    color = colors.shadeBlack4,
                    width = dimens.borderWidth
                )
                .background(color = backgroundColor)
                .clickable { widgetModel.sendAction(ConsultantAction.ChatMessageClick(message)) }
        ) {
            ChatMessageContent(message)
            ChatLabel(message)
        }
    }
}

@Composable
private fun BoxScope.ChatMessageContent(message: ChatUiEntity) {
    when (message.type) {
        WebimContentType.Jpg,
        WebimContentType.Png,
        WebimContentType.Heif -> ImageCard(message)

        WebimContentType.Pdf -> FileCard(message) {}
        else -> TextCard(message)
    }
}

@Composable
private fun BoxScope.TextCard(message: ChatUiEntity) {
    GetLocalProperties { dimens, _, _, _, _ ->
        Text(
            text = message.text,
            modifier = Modifier.Companion
                .align(Alignment.Center)
                .padding(dimens.chatCardTextContentPadding)
        )
    }
}

@Composable
private fun BoxScope.ImageCard(message: ChatUiEntity) {
    GetLocalProperties { dimens, _, _, _, _ ->
        AsyncImage(
            model = message.thumbnail,
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .requiredHeight(dimens.imageMessageSize.height)
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun BoxScope.FileCard(
    message: ChatUiEntity,
    onClick: (message: ChatUiEntity) -> Unit
) {
    GetLocalProperties { dimens, _, colors, shapes, types ->
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(dimens.chatCardFileContentPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_download),
                contentDescription = null,
                modifier = Modifier
                    .clip(shape = shapes.round)
                    .background(color = colors.gGreenB)
                    .clickable { onClick(message) }
            )
            HSpacerSmall()
            Column {
                Text(text = message.text, style = types.title3)
                Text(text = message.fileSize.orEmpty(), color = colors.gGreenB)
            }
        }
    }
}

@Composable
private fun BoxScope.ChatLabel(message: ChatUiEntity) {
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        val thumbnail = message.thumbnail
        val textColor = if (thumbnail == null) {
            colors.shadeBlack1
        } else {
            colors.white
        }
        val background = if (thumbnail == null) {
            Modifier.background(color = Color.Unspecified)
        } else {
            Modifier.background(color = colors.blackBlue, shape = shapes.round)
        }
        Row(
            modifier = Modifier
                .padding(dimens.smallDim)
                .align(Alignment.BottomEnd)
                .clip(shape = shapes.round)
                .then(background)
                .padding(dimens.chatMessageLabelPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message.date,
                color = textColor
            )
            if (message.owner == WebimOwner.User) {
                HSpacerVerySmall()
                Image(
                    painter = painterResource(
                        id = if (message.attachmentUrl != null || message.type == WebimContentType.Text) {
                            when (message.sendStatus) {
                                WebimMessageSendStatus.Sent -> R.drawable.ic_message_received
                                WebimMessageSendStatus.Sending -> R.drawable.ic_message_send
                                is WebimMessageSendStatus.Error -> R.drawable.ic_send_error
                            }
                        } else {
                            R.drawable.ic_clock
                        }
                    ),
                    colorFilter = ColorFilter.tint(
                        if (message.isRead) {
                            colors.gGreenB
                        } else {
                            colors.shadeBlack1
                        }
                    ),
                    contentDescription = null
                )
            }
        }
    }
}
