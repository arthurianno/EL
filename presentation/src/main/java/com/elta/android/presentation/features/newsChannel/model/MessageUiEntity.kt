package com.elta.android.presentation.features.newsChannel.model

import com.elta.android.presentation.features.consultant.model.CornerUiEntity
import com.elta.android.presentation.features.consultant.model.DateUiEntity
import com.elta.android.presentation.features.consultant.model.DocumentUiEntity
import java.util.UUID
import javax.annotation.concurrent.Immutable

@Immutable
data class MessageUiEntity(
    val id: UUID,
    val title: String? = null, // Добавляем поле для заголовка
    val text: String? = null,
    val image: DocumentUiEntity? = null,
    val document: DocumentUiEntity? = null,
    val sendingStatus: MessageSendStatus = MessageSendStatus.Sent,
    val timeSending: String,
    val dateSending: DateUiEntity,
    val isDayChanged: Boolean = false,
    val cornerSequence: CornerUiEntity? = null,
    val isNewMessage: Boolean = false,
    val orderNumber: Long? = null
)