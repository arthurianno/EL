package com.elta.android.presentation.features.consultant.ui.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.elta.android.domain.features.consultant.model.MessageOwner
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.disableClickable
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.HSpacerVerySmall
import com.elta.android.presentation.core.compose.widgets.VSpacerVerySmall
import com.elta.android.presentation.core.compose.widgets.animation.HorizontallyAnimation
import com.elta.android.presentation.features.consultant.model.AudioState
import com.elta.android.presentation.features.consultant.model.CachingState
import com.elta.android.presentation.features.consultant.model.DateUiEntity
import com.elta.android.presentation.features.consultant.model.DocumentUiEntity
import com.elta.android.presentation.features.consultant.model.MessageType
import com.elta.android.presentation.features.consultant.model.MessageUiEntity
import com.elta.android.presentation.features.consultant.model.toDuration
import com.elta.android.presentation.features.consultant.ui.components.animateMessageShapeAsState
import com.elta.android.presentation.theme.GetLocalProperties

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageContent(
    message: MessageUiEntity,
    onMessageClick: (MessageUiEntity) -> Unit = {},
    onLongMessageClick: (MessageUiEntity) -> Unit = {},
    onVoiceMessageIconClick: (MessageUiEntity) -> Unit = {},
    onAudioTrackClick: (MessageUiEntity, Float) -> Unit = { _, _ -> },
    onDocumentIconClick: (MessageUiEntity) -> Unit = {}
) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        val backgroundColor = remember(message) {
            when {
                message.owner == MessageOwner.Operator -> colors.white
                message.type == MessageType.Voice && message.owner == MessageOwner.User -> colors.mintBlue

                else -> colors.shadeBlack4
            }
        }
        val borderColor = remember(message.type) {
            if (message.type == MessageType.Voice) colors.gGreenB.copy(alpha = 0.1f)
            else colors.shadeBlack4
        }

        val smallCornerRadius = 4.dp
        val defaultCornerRadius = 16.dp
        val animationSpec = tween<Dp>(durationMillis = 1000)

        val topStartRadius by animateMessageShapeAsState(
            condition = message.cornerSequence?.top == true && message.owner == MessageOwner.Operator,
            targetCornerRadius = smallCornerRadius,
            defaultCornerRadius = defaultCornerRadius,
            animationSpec = animationSpec
        )
        val bottomStartRadius by animateMessageShapeAsState(
            condition = message.cornerSequence?.bottom == true && message.owner == MessageOwner.Operator,
            targetCornerRadius = smallCornerRadius,
            defaultCornerRadius = defaultCornerRadius,
            animationSpec = animationSpec
        )
        val topEndRadius by animateMessageShapeAsState(
            condition = message.cornerSequence?.top == true && message.owner == MessageOwner.User,
            targetCornerRadius = smallCornerRadius,
            defaultCornerRadius = defaultCornerRadius,
            animationSpec = animationSpec
        )
        val bottomEndRadius by animateMessageShapeAsState(
            condition = message.cornerSequence?.bottom == true && message.owner == MessageOwner.User,
            targetCornerRadius = smallCornerRadius,
            defaultCornerRadius = defaultCornerRadius,
            animationSpec = animationSpec
        )

        val shape = RoundedCornerShape(
            topStart = topStartRadius,
            bottomStart = bottomStartRadius,
            topEnd = topEndRadius,
            bottomEnd = bottomEndRadius
        )

        val modifier = Modifier
            .clip(shape = shape)
            .border(
                shape = shape,
                color = borderColor,
                width = dimens.borderWidth
            )
            .background(backgroundColor)
            .combinedClickable(
                onLongClick = {
                    if (message.owner == MessageOwner.User && message.canBeEdit)
                        onLongMessageClick(message)
                },
                onClick = {
                    if (message.type == MessageType.Image) onMessageClick(message)
                }
            )

        when (message.type) {
            MessageType.Image ->
                ImageCard(
                    message = message,
                    modifier = modifier
                )

            MessageType.Document,
            MessageType.Video -> FileCard(
                message = message,
                modifier = modifier,
                onDocumentIconClick = onDocumentIconClick
            )

            MessageType.Voice -> VoiceCard(
                message = message,
                modifier = modifier,
                onVoiceMessageIconClick = onVoiceMessageIconClick,
                onAudioTrackClick = onAudioTrackClick
            )

            MessageType.Text -> TextCard(
                message = message,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun TextCard(
    modifier: Modifier = Modifier,
    message: MessageUiEntity
) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Column(modifier.padding(dimens.chatCardTextContentPadding)) {
            Text(text = message.text.orEmpty())
            TimeLabel(
                message = message,
                textColor = colors.shadeBlack1.copy(),
                modifier = Modifier
                    .align(Alignment.End)
                    .defaultMinSize(minWidth = dimens.timeLabelWidth)
            )
        }
    }
}

// fixme: не работают размер изображение по горизонтали и вертикали
@Composable
private fun ImageCard(
    message: MessageUiEntity,
    modifier: Modifier = Modifier
) {
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        val modifierSize = if (message.document?.isPortrait == true)
            Modifier.requiredHeightIn(max = dimens.imageMessageSize.height)
        else Modifier.fillMaxWidth()
        Box(modifier = modifier.then(modifierSize)) {
            AsyncImage(
                model = if (message.owner == MessageOwner.Operator) message.document?.url
                else message.document?.uri ?: message.document?.url,
                contentScale = ContentScale.Crop,
                contentDescription = null,
                placeholder = ColorPainter(colors.shadeBlack3),
//                modifier = Modifier.matchParentSize()
            )
            TimeLabel(
                message = message,
                textColor = colors.white,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = dimens.verySmallDim, bottom = dimens.verySmallDim)
                    .clip(shape = shapes.round)
                    .background(color = colors.blackBlue)
                    .alpha(70f)
                    .padding(horizontal = dimens.smallDim, vertical = dimens.verySmallDim)
            )

        }
    }
}

@Composable
private fun FileCard(
    modifier: Modifier = Modifier,
    message: MessageUiEntity,
    onDocumentIconClick: (MessageUiEntity) -> Unit
) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Column(
            modifier = modifier
                .defaultMinSize(minWidth = 240.dp)
                .padding(dimens.chatCardFileContentPadding)
        ) {
            DocumentLabel(
                message = message,
                onDocumentDownloadClick = onDocumentIconClick
            )
            TimeLabel(
                message = message,
                textColor = colors.shadeBlack1.copy(),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun VoiceCard(
    modifier: Modifier = Modifier,
    message: MessageUiEntity,
    onVoiceMessageIconClick: (MessageUiEntity) -> Unit,
    onAudioTrackClick: (MessageUiEntity, Float) -> Unit
) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Column(
            modifier = modifier
                .defaultMinSize(minWidth = 240.dp)
                .height(IntrinsicSize.Min)
                .padding(dimens.chatCardFileContentPadding)
        ) {
            AudioTrack(
                message = message,
                onButtonClick = onVoiceMessageIconClick,
                onTrackClick = onAudioTrackClick
            )
            TimeLabel(
                message = message,
                textColor = colors.shadeBlack1.copy(),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun DocumentLabel(
    message: MessageUiEntity,
    onDocumentDownloadClick: (MessageUiEntity) -> Unit
) {
    val isUploading = message.document?.size == null
    val fileName = message.document?.fileName.takeIf { !it.isNullOrEmpty() }
        ?: stringResource(id = R.string.consultant_document_name_placeholder)
    val sizeTextId =
        if (isUploading) R.string.consultant_uploading
        else R.string.consultant_mb
    GetLocalProperties { _, _, colors, _, styles ->
        Row {
            DocumentIcon(
                message = message,
                onDocumentIconClick = onDocumentDownloadClick
            )
            HSpacerSmall()
            Column {
                Text(
                    text = fileName,
                    style = styles.title3,
                    color = colors.blackBlue,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        id = sizeTextId,
                        message.document?.size.toString()
                    ),
                    style = styles.caption1,
                    color = colors.gGreenB
                )
            }
        }
    }
}

@Composable
private fun AudioTrack(
    message: MessageUiEntity,
    onButtonClick: (MessageUiEntity) -> Unit,
    onTrackClick: (MessageUiEntity, Float) -> Unit
) {
    GetLocalProperties { dimens, _, colors, shapes, styles ->
        val (iconId, paddingValue) =
            if (message.audioState?.isPlaying == true) R.drawable.ic_audio_pause to dimens.iconPausePadding
            else R.drawable.ic_audio_play to dimens.iconPlayPadding
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier
                .size(38.dp)
                .clip(shape = shapes.round)
                .background(color = colors.gGreenB)
                .clickable { onButtonClick(message) }
                .padding(paddingValue)
                .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    tint = colors.white
                )
            }
            HSpacerSmall()
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    var seekToPosition by remember { mutableStateOf(0f) }
                    var sliderPosition by remember { mutableStateOf(0f) }
                    var audioTrackWidthPx by remember { mutableStateOf(0f) }

                    Icon(
                        painter = painterResource(id = R.drawable.img_audio_track),
                        contentDescription = null,
                        tint = colors.greenBlue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .draggable(
                                state = rememberDraggableState { delta ->
                                    sliderPosition += delta
                                    seekToPosition = sliderPosition / audioTrackWidthPx
                                },
                                orientation = Orientation.Horizontal,
                                onDragStarted = {
                                    sliderPosition = it.x
                                },
                                onDragStopped = {
                                    onTrackClick(message, seekToPosition)
                                }
                            )
                            .onGloballyPositioned { coordinates ->
                                audioTrackWidthPx = coordinates.size.width.toFloat()
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        onTrackClick(message, it.x / audioTrackWidthPx)
                                    }
                                )

                            }
                    )
                }
                VSpacerVerySmall()
                Text(
                    text = message.audioState?.duration.toDuration(),
                    style = styles.caption1,
                    color = colors.gGreenB
                )
            }
        }
    }
}

@Composable
private fun DocumentIcon(
    message: MessageUiEntity,
    onDocumentIconClick: (MessageUiEntity) -> Unit
) {
    val isDownloading = message.document?.cachingState == CachingState.Downloading
    val isUploading = message.document?.size == null

    GetLocalProperties { dimens, _, colors, shapes, _ ->
        val (colorAlpha, clickableModifier) = if (isUploading) (0.7f to Modifier.disableClickable())
        else (1f to Modifier.clickable { onDocumentIconClick(message) })
        Box(contentAlignment = Alignment.Center) {
            val iconId = when (message.document?.cachingState ?: CachingState.NotCached) {
                CachingState.Cached -> R.drawable.img_file
                CachingState.NotCached -> R.drawable.ic_arrow_download
                CachingState.Downloading -> R.drawable.ic_dialog_close
            }
            Image(
                painter = painterResource(id = iconId),
                colorFilter = ColorFilter.tint(colors.white),
                contentDescription = null,
                modifier = Modifier
                    .clip(shape = shapes.round)
                    .background(color = colors.gGreenB.copy(alpha = colorAlpha))
                    .then(clickableModifier)
                    .padding(dimens.smallDim)
            )
            AnimatedVisibility(visible = isDownloading || isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .progressSemantics()
                        .size(38.dp),
                    strokeWidth = dimens.smallestDim,
                    color = colors.white
                )
            }
        }
    }
}

@Composable
private fun TimeLabel(
    message: MessageUiEntity,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    GetLocalProperties { _, _, colors, _, styles ->
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            HorizontallyAnimation(visualState = message.isEdited) {
                Text(
                    text = stringResource(id = R.string.consultant_message_edited),
                    color = textColor,
                    style = styles.caption1
                )
            }
            HSpacerSmall()
            Text(
                text = message.timeSending,
                color = textColor,
                style = styles.caption1
            )
            if (message.owner == MessageOwner.User) {
                val messageStatusIconId = remember(message.sendingStatus) {
                    when (message.sendingStatus) {
                        WebimMessageSendStatus.Sent -> R.drawable.ic_message_received
                        WebimMessageSendStatus.Sending -> R.drawable.ic_message_send
                        is WebimMessageSendStatus.Error -> R.drawable.ic_send_error
                    }
                }
                val colorStatus = if (message.isRead) colors.gGreenB else textColor

                HSpacerVerySmall()
                Image(
                    painter = painterResource(id = messageStatusIconId),
                    colorFilter = ColorFilter.tint(colorStatus),
                    contentDescription = null
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewTextCard() {
    MessageContent(
        message = MessageUiEntity(
            id = "",
            owner = MessageOwner.User,
            type = MessageType.Text,
            document = null,
            text = "Да",
            sendingStatus = WebimMessageSendStatus.Sending,
            isRead = false,
            timeSending = "11:11",
            isEdited = true,
            canBeEdit = true,
            audioState = null,
            cornerSequence = null,
            isDayChanged = false,
            dateSending = DateUiEntity.Today(100)
        )
    )
}

@Preview
@Composable
private fun PreviewImageCard() {
    MessageContent(
        message = MessageUiEntity(
            id = "",
            owner = MessageOwner.User,
            type = MessageType.Image,
            document = DocumentUiEntity(
                fileName = "",
                fileType = MessageType.Image,
                url = "https://masterpiecer-images.s3.yandex.net/c352b1b9801c11ee9607720ccb3e265f:upscaled",
                size = null,
                isPortrait = true,
                cachingState = CachingState.Cached,
                uri = null
            ),
            text = "Some text for example",
            sendingStatus = WebimMessageSendStatus.Sent,
            isRead = false,
            timeSending = "11:11",
            isEdited = false,
            canBeEdit = true,
            audioState = null,
            cornerSequence = null,
            isDayChanged = false,
            dateSending = DateUiEntity.Today(100)
        )
    )
}

@Preview
@Composable
private fun PreviewFileCard() {
    MessageContent(
        message = MessageUiEntity(
            id = "",
            owner = MessageOwner.User,
            type = MessageType.Document,
            document = DocumentUiEntity(
                fileName = "document",
                fileType = MessageType.Document,
                url = null,
                size = 1.0,
                isPortrait = null,
                cachingState = CachingState.Cached,
                uri = null
            ),
            text = "documentFile",
            sendingStatus = WebimMessageSendStatus.Sent,
            isRead = false,
            timeSending = "11:11",
            isEdited = false,
            canBeEdit = true,
            cornerSequence = null,
            isDayChanged = false,
            dateSending = DateUiEntity.Today(100),
            audioState = null
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewVoiceCard() {
    Box(Modifier.padding(8.dp)) {
        MessageContent(
            message = MessageUiEntity(
                id = "",
                owner = MessageOwner.User,
                type = MessageType.Voice,
                document = null,
                audioState = AudioState(
                    isPlaying = false,
                    trackPosition = 10,
                    duration = 10
                ),
                text = "documentFile",
                sendingStatus = WebimMessageSendStatus.Sent,
                isRead = false,
                timeSending = "11:11",
                isEdited = false,
                canBeEdit = true,
                cornerSequence = null,
                isDayChanged = false,
                dateSending = DateUiEntity.Today(100)
            )
        )
    }
}
