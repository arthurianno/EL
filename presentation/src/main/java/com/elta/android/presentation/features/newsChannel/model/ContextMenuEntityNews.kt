package com.elta.android.presentation.features.newsChannel.model

import androidx.compose.runtime.Immutable

@Immutable
data class ContextMenuUiEntityNews(
    val isOpenContextMenu: Boolean,
    val selectedMessage: MessageUiEntity?
)