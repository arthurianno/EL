package com.elta.android.domain.features.consultant.repository

import com.elta.android.common.repository.BaseRepository
import com.elta.android.domain.features.consultant.model.WebimChatState
import com.elta.android.domain.features.consultant.model.WebimMessage
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
    fun chatState(): Flow<WebimChatState>
    fun chatNetworkStatus(): Flow<WebimStatus>
    val messages: Flow<List<WebimMessage>>
}
