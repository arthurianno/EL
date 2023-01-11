package com.elta.android.presentation.features.consultant.model

import androidx.compose.runtime.Immutable
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.domain.features.consultant.model.WebimMessageType
import com.elta.android.domain.features.consultant.model.WebimOwner

@Immutable
data class ChatUiEntity(
    val owner: WebimOwner,
    val type: WebimMessageType,
    val text: String,
    val date: String,
    val sendStatus: WebimMessageSendStatus,
    val isRead: Boolean
)
