package com.elta.android.domain.features.consultant.repository

import com.elta.android.common.repository.BaseRepository
import com.elta.android.domain.features.consultant.model.BotNode
import com.elta.android.domain.features.consultant.model.ChatMessage
import com.elta.android.domain.features.consultant.model.UserState
import kotlinx.coroutines.flow.Flow

interface ConsultantRepository : BaseRepository {
    suspend fun getRootNode(): BotNode
    suspend fun getNodeById(id: String): BotNode?
    suspend fun searchNodeByText(query: String): BotNode?

    fun getMessagesFlow(): Flow<List<ChatMessage>>
    suspend fun saveMessage(message: ChatMessage)
    suspend fun clearHistory()

    suspend fun getBotState(): UserState
    suspend fun saveBotState(state: UserState)
    suspend fun clearBotState()
}
