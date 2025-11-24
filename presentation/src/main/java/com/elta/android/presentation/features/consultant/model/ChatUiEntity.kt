package com.elta.android.presentation.features.consultant.model

import androidx.compose.runtime.Immutable

@Immutable
data class ChatUiEntity(
    val messages: List<MessageUiEntity>,
    val ratingEntity: RatingUiEntity
)
