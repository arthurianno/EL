package com.elta.android.domain.features.consultant.repository

import com.elta.android.common.repository.BaseRepository
import com.elta.android.domain.features.consultant.model.ChatState
import com.elta.android.domain.features.consultant.model.ConnectionStatus
import com.elta.android.domain.features.consultant.model.ConsultantMessage
import com.elta.android.domain.features.consultant.model.ConsultantChat
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.domain.features.consultant.model.WebimUser
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ConsultantRepository : BaseRepository {
    fun webimSessionCreate(webimUser: WebimUser, firebaseToken: String?)
    fun webimResume()
    fun webimPause()
    fun webimDestroy()
    fun startChat()
    suspend fun sendMessage(message: String)
    suspend fun editMessage(id: String, newText: String)
    suspend fun deleteMessage(id: String)
    suspend fun sendFile(file: File?): Flow<WebimMessageSendStatus>
    suspend fun sendRate(rateNumber: Int)
    suspend fun loadLastMessages(size: Int): List<ConsultantMessage>
    suspend fun loadNextCachedMessages(size: Int): List<ConsultantMessage>
    fun chatState(): Flow<ChatState>
    fun chatNetworkStatus(): Flow<ConnectionStatus>
    val chat: Flow<ConsultantChat>
}

