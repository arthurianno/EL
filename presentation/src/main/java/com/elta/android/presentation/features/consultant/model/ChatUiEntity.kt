package com.elta.android.presentation.features.consultant.model

import androidx.compose.runtime.Immutable
import com.elta.android.domain.features.consultant.model.WebimContentType
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.domain.features.consultant.model.WebimOwner

@Immutable
data class ChatUiEntity(
    val owner: WebimOwner,
    val type: WebimContentType?,
    val thumbnail: String?,
    val attachmentUrl: String?,
    val fileSize: String?,
    val text: String,
    val date: String,
    val sendStatus: WebimMessageSendStatus,
    val isRead: Boolean
)
