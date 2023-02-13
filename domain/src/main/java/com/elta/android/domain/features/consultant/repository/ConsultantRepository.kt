package com.elta.android.domain.features.consultant.repository

import android.graphics.Bitmap
import android.net.Uri
import com.elta.android.common.repository.BaseRepository
import com.elta.android.domain.common.model.FileType
import com.elta.android.domain.features.consultant.model.ChatList
import com.elta.android.domain.features.consultant.model.WebimChatState
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.domain.features.consultant.model.WebimStatus
import com.elta.android.domain.features.consultant.model.WebimUser
import kotlinx.coroutines.flow.Flow
import java.io.File

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
    suspend fun deleteFile(uri: Uri)
    suspend fun cachedPhoto(name: String, bitmap: Bitmap)
    suspend fun cachedFile(cacheName: String, fileType: FileType, sourceUri: Uri): Uri
    suspend fun clearCache()
    fun createAudioFile(): File
}
