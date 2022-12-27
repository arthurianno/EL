package com.elta.android.presentation.features.consultant.model

import com.elta.android.domain.features.consultant.model.WebimMessageType
import com.elta.android.domain.features.consultant.model.WebimOwner

data class ChatUiEntity(
    val owner: WebimOwner,
    val type: WebimMessageType,
    val text: String
)
