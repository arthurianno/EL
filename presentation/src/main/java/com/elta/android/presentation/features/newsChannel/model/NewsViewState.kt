package com.elta.android.presentation.features.newsChannel.model

import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.RecordGraphState
import androidx.compose.runtime.Immutable
import com.elta.android.presentation.features.consultant.model.PreviewState

@Immutable
data class NewsViewState(
    val inputMessage: String,
    val recordGraphState: RecordGraphState,
    val connectState: ConnectState,
    val chat: ChatUiEntity,
    val previewState: PreviewStateNews,
    val hasNewMessages: Boolean,
    val isOpenBottomSheet: Boolean,
    val contextMenuEntity: ContextMenuUiEntityNews,
    val isLoadingNextMessagesPage: Boolean,
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val isSwipeRefreshing: Boolean = false,
)
