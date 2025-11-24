package com.elta.android.presentation.features.newsChannel.ui.components.content

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.DateUiEntity
import com.elta.android.presentation.features.newsChannel.model.ChatUiEntity
import com.elta.android.presentation.features.newsChannel.model.ContextMenuUiEntityNews
import com.elta.android.presentation.features.newsChannel.model.MessageUiEntity
import com.elta.android.presentation.theme.GetLocalProperties
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewsChatContent(
    modifier: Modifier = Modifier,
    chat: ChatUiEntity,
    connectState: ConnectState,
    contentMenuEntity: ContextMenuUiEntityNews,
    isLoadingNextMessagesPage: Boolean,
    isSwipeRefreshing: Boolean,
    hasNewMessages: Boolean,
    listState: LazyListState,
    onMessageClick: (MessageUiEntity) -> Unit = {},
    onLongMessageClick: (MessageUiEntity) -> Unit = {},
    onDocumentIconClick: (MessageUiEntity) -> Unit = {},
    onCopyClick: (MessageUiEntity) -> Unit = {},
    onDismissClick: () -> Unit = {},
    onSwipeRefresh: () -> Unit = {},
    onDownIconClick: () -> Unit = {},
    onScrollToTop: () -> Unit = {},
    onLoadNextPage: () -> Unit = {}
) {
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    GetLocalProperties { _, _, colors, _, _ ->
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isSwipeRefreshing,
            onRefresh = onSwipeRefresh
        )
        Box(
            modifier = modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            when {
                // Проверка на отсутствие интернета
                connectState == ConnectState.Offline -> {
                    Text(
                        text = stringResource(id = R.string.news_chat_noConnect_text),
                        color = colors.shadeBlack2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                isLoadingNextMessagesPage && chat.messages.isEmpty() -> {
                    Text(
                        text = stringResource(id = R.string.news_chat_loading_text),
                        color = colors.shadeBlack2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                !isLoadingNextMessagesPage && chat.messages.isEmpty() -> {
                    Text(
                        text = stringResource(id = R.string.news_chat_empty_text),
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
                        onMessageClick = onMessageClick,
                        onDocumentIconClick = onDocumentIconClick,
                        onLongMessageClick = onLongMessageClick,
                        onCopyClick = onCopyClick,
                        onDownIconClick = onDownIconClick,
                        onDismissClick = onDismissClick,
                        onScrollToTop = onScrollToTop,
                        onLoadNextPage = onLoadNextPage
                    )
                }
            }
            // Изолируем AnimatedVisibility для минимизации recomposition
            AnimatedVisibility(
                visible = isLoadingNextMessagesPage && chat.messages.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Log.d("NewsChatContent", "Showing 'Грузим новости...' at bottom")
                Text(
                    text = "Грузим новости...",
                    color = colors.shadeBlack2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(colors.paleGrayDark)
                        .padding(8.dp)
                )
            }
            PullRefreshIndicator(
                refreshing = isSwipeRefreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
            )
        }
    }
}

@Composable
fun Message(
    message: MessageUiEntity,
    onMessageClick: (MessageUiEntity) -> Unit = {},
    onLongMessageClick: (MessageUiEntity) -> Unit = {},
    onCopyClick: (MessageUiEntity) -> Unit = {},
    onDocumentIconClick: (MessageUiEntity) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
    ) {
        if (message.isDayChanged) {
            DateLabel(
                value = message.dateSending,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(bottom = 4.dp, top = 8.dp)
            )
        }
        if (message.isNewMessage) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                MessageRow(
                    message = message,
                    onMessageClick = onMessageClick,
                    onLongMessageClick = onLongMessageClick,
                    onCopyClick = onCopyClick,
                    onDocumentIconClick = onDocumentIconClick
                )
            }
        } else {
            MessageRow(
                message = message,
                onMessageClick = onMessageClick,
                onLongMessageClick = onLongMessageClick,
                onCopyClick = onCopyClick,
                onDocumentIconClick = onDocumentIconClick
            )
        }
    }
}

@Composable
private fun ChatScreen(
    listState: LazyListState,
    chat: ChatUiEntity,
    hasNewMessages: Boolean,
    connectState: ConnectState,
    isKeyboardVisible: Boolean,
    contentMenuEntity: ContextMenuUiEntityNews,
    onMessageClick: (MessageUiEntity) -> Unit = {},
    onDocumentIconClick: (MessageUiEntity) -> Unit = {},
    onLongMessageClick: (MessageUiEntity) -> Unit = {},
    onCopyClick: (MessageUiEntity) -> Unit = {},
    onDownIconClick: () -> Unit = {},
    onDismissClick: () -> Unit = {},
    isLoadingNextMessagesPage: Boolean,
    onScrollToTop: () -> Unit = {},
    onLoadNextPage: () -> Unit = {}
) {
    var isInitialLoad by remember { mutableStateOf(true) }
    var scrollTo by remember { mutableStateOf<Int?>(null) }

    val isLastItemVisible by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val visibleItems = layoutInfo.visibleItemsInfo
            if (totalItemsCount == 0) false
            else visibleItems.lastOrNull()?.index == totalItemsCount - 1
        }
    }
    val isFirstItemVisible by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount == 0) false
            else visibleItems.firstOrNull()?.index == 0
        }
    }

    LaunchedEffect(
        scrollTo,
        chat.messages.size,
        isLastItemVisible,
        isLoadingNextMessagesPage,
        contentMenuEntity.isOpenContextMenu,
        isFirstItemVisible
    ) {
        scrollTo?.let { index ->
            listState.animateScrollToItem(index)
            scrollTo = null
        }

        if (chat.messages.isNotEmpty()) {
            delay(500)
            isInitialLoad = false
        }

        if (isLastItemVisible && !isLoadingNextMessagesPage && !isInitialLoad && chat.messages.size >= 3) {
            delay(200)
            Log.d("ChatScreen", "Last item visible, triggering load next page")
            onLoadNextPage()
        }

        contentMenuEntity.selectedMessage?.let {
            val selectedItemIndex = chat.messages.indexOf(it)
            if (selectedItemIndex != -1) {
                delay(200)
                listState.animateScrollToItem(index = selectedItemIndex)
            }
        }

        if (isFirstItemVisible && !isLoadingNextMessagesPage && scrollTo == 0) {
            delay(200)
            onScrollToTop()
        }
    }

    GetLocalProperties { dimens, _, colors, _, _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomCenter),
                state = listState,
                contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = chat.messages,
                    key = { _, message -> message.id.toString() }
                ) { _, message ->
                    Message(
                        message = message,
                        onMessageClick = onMessageClick,
                        onLongMessageClick = onLongMessageClick,
                        onCopyClick = onCopyClick,
                        onDocumentIconClick = onDocumentIconClick
                    )
                }
            }
            AnimatedVisibility(
                visible = !isLastItemVisible && !isKeyboardVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 300, easing = EaseInOut)) +
                        slideInVertically(
                            animationSpec = tween(durationMillis = 300, easing = EaseInOut),
                            initialOffsetY = { it / 2 }
                        ),
                exit = fadeOut(animationSpec = tween(durationMillis = 300, easing = EaseInOut)),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                DownIcon(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    onIconClick = {
                        Log.d("ChatScreen", "DownIcon clicked")
                        scrollTo = chat.messages.size - 1
                        onDownIconClick()
                    }
                )
            }
            AnimatedVisibility(
                visible = !isFirstItemVisible && !isKeyboardVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 300, easing = EaseInOut)) +
                        slideInVertically(
                            animationSpec = tween(durationMillis = 300, easing = EaseInOut),
                            initialOffsetY = { -it / 2 }
                        ),
                exit = fadeOut(animationSpec = tween(durationMillis = 300, easing = EaseInOut)),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                UpIcon(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    onIconClick = {
                        Log.d("ChatScreen", "UpIcon clicked")
                        scrollTo = 0
                        onScrollToTop()
                    }
                )
            }
            AnimatedVisibility(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.BottomEnd),
                visible = contentMenuEntity.isOpenContextMenu,
                enter = fadeIn(animationSpec = tween(100)),
                exit = fadeOut()
            ) {
                Popup(
                    alignment = Alignment.BottomEnd,
                    onDismissRequest = onDismissClick
                ) {
                    SelectedContentNews(
                        contentMenuEntity = contentMenuEntity,
                        connectState = connectState,
                        onCopyClick = onCopyClick
                    )
                }
            }
        }
    }
}
@Composable
private fun MessageRow(
    message: MessageUiEntity,
    onMessageClick: (MessageUiEntity) -> Unit,
    onLongMessageClick: (MessageUiEntity) -> Unit,
    onCopyClick: (MessageUiEntity) -> Unit,
    onDocumentIconClick: (MessageUiEntity) -> Unit
) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 0.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 400.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VSpacerSmall()
                NewsMessageContent(
                    message = message,
                    onMessageClick = onMessageClick,
                    onDocumentIconClick = onDocumentIconClick
                )
            }
        }
    }
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
                .clickable { onIconClick() }
        )
    }
}

@Composable
private fun UpIcon(
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
                .graphicsLayer(rotationZ = 180f)
                .clickable { onIconClick() }
        )
    }
}