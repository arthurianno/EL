package com.elta.android.presentation.features.consultant.model

import androidx.compose.runtime.Immutable
import com.elta.android.domain.features.consultant.model.BotOption
import com.elta.android.domain.features.consultant.model.ChatMessage

@Immutable
data class ConsultantViewState(
    val chatMessages: List<ChatMessage>,
    val currentOptions: List<BotOption>,
    val isBotTyping: Boolean,
    val canGoBack: Boolean
)
