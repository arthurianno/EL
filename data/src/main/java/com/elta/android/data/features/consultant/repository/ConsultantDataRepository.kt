package com.elta.android.data.features.consultant.repository

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.MimeTypeMap
import com.elta.android.data.features.common.storage.FileStorage
import com.elta.android.data.features.consultant.datasource.WebimDataSource
import com.elta.android.domain.common.addExtension
import com.elta.android.domain.common.model.FileType
import com.elta.android.domain.features.consultant.model.ChatList
import com.elta.android.domain.features.consultant.model.WebimChatState
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.domain.features.consultant.model.WebimStatus
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.webim.android.sdk.Message
import ru.webim.android.sdk.MessageStream.SendFileCallback
import ru.webim.android.sdk.WebimError
import ru.webim.android.sdk.WebimSession
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val PHOTO_NAME_PREFIX = "eltaPhoto_"
private const val PHOTO_NAME_PATTERN = "yyyyMMdd_HHmmss"
private const val START_PROGRESS = 0f
private const val END_PROGRESS = 1f
private const val NO_CACHE_FILE_EXIST = "File not found in the cache!!! , Filename ----> "

class ConsultantDataRepository @Inject constructor(
    private val webimDataSource: WebimDataSource,
    private val fileStorage: FileStorage,
    override val dispatcher: CoroutineDispatcher
) : ConsultantRepository {

    private var webimSession: WebimSession? = null

    override fun webimSessionCreate(webimUser: WebimUser) {
        webimSession = webimDataSource.sessionCreate(webimUser)
    }

    override fun webimResume() {
        webimSession?.resume()
    }

    override fun webimPause() {
        webimSession?.pause()
    }

    override fun webimDestroy() {
        webimSession?.destroy()
    }

    override fun startChat() {
        webimSession?.stream?.startChat()
    }

    override suspend fun sendMessage(message: String) {
        webimSession?.stream?.sendMessage(message)
    }

    override fun sendFile(fileName: String): Flow<WebimMessageSendStatus> {
        val sendFlow = MutableStateFlow<WebimMessageSendStatus>(WebimMessageSendStatus.Sending)
        fileStorage.getCacheFile(fileName)?.let { file ->
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)?.let { mimeType ->
                webimSession?.stream?.sendFile(
                    file,
                    file.name,
                    mimeType,
                    object : SendFileCallback {
                        @Deprecated("Deprecated in Java")
                        override fun onProgress(id: Message.Id, sentBytes: Long) {
                        }

                        override fun onSuccess(id: Message.Id) {
                            sendFlow.tryEmit(WebimMessageSendStatus.Sent)
                        }

                        override fun onFailure(
                            id: Message.Id,
                            error: WebimError<SendFileCallback.SendFileError>
                        ) {
                            sendFlow.tryEmit(WebimMessageSendStatus.Error(error.errorString))
                        }
                    }
                )
            }
        } ?: run {
            Timber.e("$NO_CACHE_FILE_EXIST$fileName")
            sendFlow.tryEmit(WebimMessageSendStatus.Error(NO_CACHE_FILE_EXIST))
        }
        return sendFlow
    }

    override fun chatState(): Flow<WebimChatState> =
        webimDataSource.webimChatState

    override fun chatNetworkStatus(): Flow<WebimStatus> =
        webimDataSource.webimNetworkStatus

    override val chat: Flow<ChatList> =
        webimDataSource.chat

    override fun createPhoto(): Uri =
        fileStorage.createCachedPhoto(
            PHOTO_NAME_PREFIX + SimpleDateFormat(
                PHOTO_NAME_PATTERN,
                Locale.getDefault()
            ).format(Date())
        )

    override suspend fun deletePhoto(uri: Uri) {
        fileStorage.deleteFile(uri)
    }

    override suspend fun cachedPhoto(name: String, bitmap: Bitmap) {
        fileStorage.createCachedPhoto(fileName = name, bitmap = bitmap)
    }

    override suspend fun cachedFile(cacheName: String, fileType: FileType, sourceUri: Uri): Uri =
        fileStorage.createCachedFile(
            cachedFileName = cacheName addExtension fileType,
            sourceUri = sourceUri
        )

    override suspend fun clearCache() {
        fileStorage.getAllCache()
            .forEach {
                fileStorage.deleteFile(fileStorage.getFileUri(it))
            }
    }
}
