package com.elta.android.presentation.features.newsChannel.model

import androidx.compose.runtime.Immutable


@Immutable
data class ChatUiEntity(
    val messages: List<MessageUiEntity>
)