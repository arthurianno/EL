package com.elta.android.presentation.features.consultant.ui.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.elta.android.domain.features.consultant.model.MessageOwner
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacerHalfMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.animation.VerticallyAnimation
import com.elta.android.presentation.features.consultant.model.ChatUiEntity
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.ContextMenuUiEntity
import com.elta.android.presentation.features.consultant.model.DateUiEntity
import com.elta.android.presentation.features.consultant.model.MessageType
import com.elta.android.presentation.features.consultant.model.MessageUiEntity
import com.elta.android.presentation.features.consultant.model.RatingUiEntity
import com.elta.android.presentation.theme.GetLocalProperties

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatContent(
    modifier: Modifier = Modifier,
    chat: ChatUiEntity,
    connectState: ConnectState,
    contentMenuEntity: ContextMenuUiEntity,
    isLoadingNextMessagesPage: Boolean,
    hasNewMessages: Boolean,
    listState: LazyListState,
    onMessageClick: (MessageUiEntity) -> Unit = {},
    onLongMessageClick: (MessageUiEntity) -> Unit = {},
    onDocumentIconClick: (MessageUiEntity) -> Unit = {},
    onVoiceMessageIconClick: (MessageUiEntity) -> Unit = {},
    onAudioTrackClick: (MessageUiEntity, Float) -> Unit = { _, _ -> },
    onRatingStarIconClick: (Int) -> Unit = {},
    onCopyClick: (MessageUiEntity) -> Unit = {},
    onEditClick: (MessageUiEntity) -> Unit = {},
    onDeleteClick: (MessageUiEntity) -> Unit = {},
    onDismissClick: () -> Unit = {},
    onSwipeRefresh: () -> Unit = {},
    onDownIconClick: () -> Unit = {}
) {
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    GetLocalProperties { _, _, colors, _, _ ->
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isLoadingNextMessagesPage,
            onRefresh = onSwipeRefresh
        )
        Box(
            modifier = modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            when {
                connectState == ConnectState.Offline -> {
                    OfflineScreen(modifier = Modifier.align(Alignment.Center))
                }

                chat.messages.isEmpty() -> {
                    Text(
                        text = stringResource(id = R.string.consultant_chat_empty_text),
                        color = colors.shadeBlack2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    ChatScreen(
                        listState = listState,
                        chat = chat,
                        isKeyboardVisible = isKeyboardVisible,
                        hasNewMessages = hasNewMessages,
                        contentMenuEntity = contentMenuEntity,
                        connectState = connectState,
                        isLoadingNextMessagesPage = isLoadingNextMessagesPage,
                        pullRefreshState = pullRefreshState,
                        onMessageClick = onMessageClick,
                        onDocumentIconClick = onDocumentIconClick,
                        onVoiceMessageIconClick = onVoiceMessageIconClick,
                        onAudioTrackClick = onAudioTrackClick,
                        onLongMessageClick = onLongMessageClick,
                        onRatingStarIconClick = onRatingStarIconClick,
                        onDownIconClick = onDownIconClick,
                        onDismissClick = onDismissClick,
                        onCopyClick = onCopyClick,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick
                    )
                }
            }
        }
    }
}

@Composable
fun OfflineScreen(modifier: Modifier = Modifier) {
    GetLocalProperties { _, _, colors, _, styles ->
        Column(modifier = modifier) {
            Text(
                text = stringResource(id = R.string.consultant_cant_load_messages),
                style = styles.body1,
                color = colors.blackBlue,
                textAlign = TextAlign.Center
            )
            VSpacerHalfMedium()
            Text(
                text = stringResource(id = R.string.consultant_repeat_load_messages),
                style = styles.title3,
                color = colors.greenBlue
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ChatScreen(
    listState: LazyListState,
    chat: ChatUiEntity,
    hasNewMessages: Boolean,
    connectState: ConnectState,
    isKeyboardVisible: Boolean,
    contentMenuEntity: ContextMenuUiEntity,
    isLoadingNextMessagesPage: Boolean,
    pullRefreshState: PullRefreshState,
    onMessageClick: (MessageUiEntity) -> Unit = {},
    onDocumentIconClick: (MessageUiEntity) -> Unit = {},
    onVoiceMessageIconClick: (MessageUiEntity) -> Unit = {},
    onAudioTrackClick: (MessageUiEntity, Float) -> Unit = { _, _ -> },
    onLongMessageClick: (MessageUiEntity) -> Unit = {},
    onRatingStarIconClick: (Int) -> Unit = {},
    onDownIconClick: () -> Unit = {},
    onDismissClick: () -> Unit = {},
    onCopyClick: (MessageUiEntity) -> Unit = {},
    onEditClick: (MessageUiEntity) -> Unit = {},
    onDeleteClick: (MessageUiEntity) -> Unit = {}
) {
    val isLastItemVisible by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val visibleItems = layoutInfo.visibleItemsInfo

            if (totalItemsCount == 0) false
            else {
                visibleItems.lastOrNull()?.index == totalItemsCount - 1
            }
        }
    }
    LaunchedEffect(key1 = hasNewMessages) {
        if (hasNewMessages && !contentMenuEntity.isOpenContextMenu) {
            listState.animateScrollToItem(chat.messages.size)
        }
    }
    LaunchedEffect(key1 = contentMenuEntity.isOpenContextMenu) {
        contentMenuEntity.selectedMessage?.let {
            val selectedItemIndex = chat.messages.indexOf(it)
            listState.animateScrollToItem(index = selectedItemIndex)
        }
    }

    GetLocalProperties { dimens, _, _, _, _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomCenter),
                state = listState,
                contentPadding = dimens.chatPadding,
                verticalArrangement = Arrangement.Bottom
            ) {
                items(
                    items = chat.messages,
                    key = { it.id }
                ) { message ->
                    Message(
                        message = message,
                        onMessageClick = onMessageClick,
                        onLongMessageClick = onLongMessageClick,
                        onDocumentIconClick = onDocumentIconClick,
                        onVoiceMessageIconClick = onVoiceMessageIconClick,
                        onAudioTrackClick = onAudioTrackClick
                    )
                }
                item {
                    AnimatedVisibility(
                        visible = chat.ratingEntity.isRatingMessageShowing,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        RatingContent(
                            starsCount = chat.ratingEntity.starsCount,
                            onRatingStarIconClick = onRatingStarIconClick
                        )
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = isLoadingNextMessagesPage,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            AnimatedVisibility(
                visible = !isLastItemVisible && !isKeyboardVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                DownIcon(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    onIconClick = onDownIconClick
                )
            }
            AnimatedVisibility(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.BottomEnd),
                visible = (contentMenuEntity.selectedMessage?.owner == MessageOwner.User &&
                        contentMenuEntity.isOpenContextMenu),
                enter = fadeIn(animationSpec = tween(100)),
                exit = fadeOut()
            ) {
                Popup(
                    alignment = Alignment.BottomEnd,
                    onDismissRequest = onDismissClick
                ) {
                    SelectedContent(
                        contentMenuEntity = contentMenuEntity,
                        connectState = connectState,
                        onCopyClick = onCopyClick,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick
                    )
                }
            }
        }
    }
}

@Composable
fun Message(
    message: MessageUiEntity,
    onMessageClick: (MessageUiEntity) -> Unit = {},
    onLongMessageClick: (MessageUiEntity) -> Unit = {},
    onDocumentIconClick: (MessageUiEntity) -> Unit = {},
    onVoiceMessageIconClick: (MessageUiEntity) -> Unit = {},
    onAudioTrackClick: (MessageUiEntity, Float) -> Unit = { _, _ -> }
) {
    Column(modifier = Modifier.wrapContentWidth()) {
        VerticallyAnimation(
            visualState = message.isDayChanged,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 4.dp, top = 8.dp)
        ) {
            DateLabel(
                value = message.dateSending,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        MessageRow(
            message = message,
            onMessageClick = onMessageClick,
            onLongMessageClick = onLongMessageClick,
            onDocumentIconClick = onDocumentIconClick,
            onVoiceMessageIconClick = onVoiceMessageIconClick,
            onAudioTrackClick = onAudioTrackClick
        )
    }
}

@Composable
private fun MessageRow(
    message: MessageUiEntity,
    onMessageClick: (MessageUiEntity) -> Unit,
    onLongMessageClick: (MessageUiEntity) -> Unit,
    onDocumentIconClick: (MessageUiEntity) -> Unit,
    onVoiceMessageIconClick: (MessageUiEntity) -> Unit = {},
    onAudioTrackClick: (MessageUiEntity, Float) -> Unit = { _, _ -> }
) {
    val isOperator = message.owner == MessageOwner.Operator

    GetLocalProperties { dimens, _, colors, _, _ ->

        val (contentAlignment, padding) = remember {
            if (isOperator) (Alignment.Start to dimens.charCardOperatorMessagePadding)
            else (Alignment.End to dimens.charCardUserMessagePadding)
        }

        Row(
            modifier = Modifier
                .padding(padding)
                .wrapContentWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            if (isOperator) {
                OperatorImage()
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = contentAlignment
            ) {
                if (isOperator && message.cornerSequence?.top == false) {
                    OperatorNameLabel()
                }
                VSpacerSmall()
                MessageContent(
                    message = message,
                    onMessageClick = onMessageClick,
                    onLongMessageClick = onLongMessageClick,
                    onDocumentIconClick = onDocumentIconClick,
                    onVoiceMessageIconClick = onVoiceMessageIconClick,
                    onAudioTrackClick = onAudioTrackClick
                )
            }
        }
    }
}

@Composable
fun OperatorNameLabel() {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Text(
            modifier = Modifier.padding(horizontal = dimens.halfMediumDim),
            text = stringResource(id = R.string.consultant_operator_name),
            color = colors.shadeBlack2
        )
    }
}

@Composable
fun OperatorImage() {
    Image(
        painter = painterResource(id = R.drawable.img_round_elta),
        contentDescription = null
    )
    HSpacerSmall()
}

@Composable
private fun DateLabel(
    modifier: Modifier = Modifier,
    value: DateUiEntity
) {
    val date = when (value) {
        is DateUiEntity.Today -> stringResource(id = R.string.consultant_message_date_today)
        is DateUiEntity.Yesterday -> stringResource(id = R.string.consultant_message_date_yesterday)
        is DateUiEntity.ThisYear -> value.date
        is DateUiEntity.Other -> value.date
    }

    GetLocalProperties { dimens, _, colors, shapes, styles ->
        Text(
            text = date,
            color = colors.shadeBlack1,
            style = styles.textStyle2,
            modifier = modifier
                .background(
                    color = colors.paleGrayDark,
                    shape = shapes.consultantTextField
                )
                .padding(dimens.dishCheckProduct)
        )
    }
}

@Composable
private fun DownIcon(
    modifier: Modifier = Modifier,
    onIconClick: () -> Unit
) {
    GetLocalProperties { _, _, colors, _, _ ->
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_down),
            contentDescription = null,
            tint = colors.white,
            modifier = modifier
                .padding(8.dp)
                .size(38.dp)
                .clip(shape = CircleShape)
                .background(colors.shadeBlack1)
                .clickable(onClick = onIconClick)
        )
    }
}

@Preview
@Composable
private fun PreviewDownIcon() {
    DownIcon {}
}

@Preview
@Composable
private fun PreviewChatComponent() {
    val message = MessageUiEntity(
        id = "",
        text = "Привет",
        document = null,
        owner = MessageOwner.User,
        sendingStatus = WebimMessageSendStatus.Sent,
        timeSending = "11:11",
        type = MessageType.Text,
        isRead = true,
        isEdited = true,
        canBeEdit = true,
        audioState = null,
        cornerSequence = null,
        isDayChanged = true,
        dateSending = DateUiEntity.Today(1721077200000)
    )

    ChatContent(
        modifier = Modifier,
        chat = ChatUiEntity(
            listOf(
                message,
                message.copy(owner = MessageOwner.Operator)
            ),
            RatingUiEntity(isRatingMessageShowing = false, starsCount = null)
        ),
        connectState = ConnectState.Connect,
        contentMenuEntity = ContextMenuUiEntity(
            isOpenContextMenu = false,
            selectedMessage = null
        ),
        isLoadingNextMessagesPage = false,
        hasNewMessages = true,
        listState = LazyListState()
    )
}

@Preview
@Composable
private fun PreviewDateLabel() {
    DateLabel(value = DateUiEntity.Today(100))
}
