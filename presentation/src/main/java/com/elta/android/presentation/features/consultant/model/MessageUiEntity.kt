package com.elta.android.presentation.features.consultant.model

import com.elta.android.domain.features.consultant.model.MessageOwner
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import javax.annotation.concurrent.Immutable

@Immutable
data class MessageUiEntity(
    val id : String,
    val text : String?,
    val document: DocumentUiEntity?,
    val owner: MessageOwner,
    val sendingStatus: WebimMessageSendStatus,
    val timeSending: String,
    val dateSending: DateUiEntity,
    val type: MessageType,
    val isRead: Boolean,
    val isEdited: Boolean,
    val canBeEdit: Boolean,
    val isDayChanged: Boolean,
    val cornerSequence: CornerUiEntity?,
    val audioState: AudioState?,
)
