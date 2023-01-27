package com.elta.android.domain.features.consultant.repository

import android.net.Uri
import com.elta.android.common.repository.BaseRepository
import com.elta.android.domain.features.consultant.model.ChatList
import com.elta.android.domain.features.consultant.model.WebimChatState
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.domain.features.consultant.model.WebimStatus
import com.elta.android.domain.features.consultant.model.WebimUser
import kotlinx.coroutines.flow.Flow

interface ConsultantRepository : BaseRepository {
    fun webimSessionCreate(webimUser: WebimUser)
    fun webimResume()
    fun webimPause()
    fun webimDestroy()
    fun startChat()
    suspend fun sendMessage(message: String)
    fun sendFile(fileName: String): Flow<WebimMessageSendStatus>
    fun chatState(): Flow<WebimChatState>
    fun chatNetworkStatus(): Flow<WebimStatus>
    val chat: Flow<ChatList>

    fun createPhoto(): Uri
    fun deletePhoto(uri: Uri)
}
