package com.elta.android.data.features.consultant.repository

import android.net.Uri
import com.elta.android.data.features.common.storage.FileStorage
import com.elta.android.data.features.consultant.datasource.WebimDataSource
import com.elta.android.domain.features.consultant.model.ChatList
import com.elta.android.domain.features.consultant.model.WebimChatState
import com.elta.android.domain.features.consultant.model.WebimStatus
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import ru.webim.android.sdk.WebimSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val PHOTO_NAME_PREFIX = "eltaPhoto_"
private const val PHOTO_NAME_PATTERN = "yyyyMMdd_HHmmss"

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

    override fun chatState(): Flow<WebimChatState> =
        webimDataSource.webimChatState

    override fun chatNetworkStatus(): Flow<WebimStatus> =
        webimDataSource.webimNetworkStatus

    override val chat: Flow<ChatList> =
        webimDataSource.chat

    override fun createPhoto(): Uri =
        with(fileStorage) {
            getFileUri(
                createJpgFile(
                    PHOTO_NAME_PREFIX + SimpleDateFormat(
                        PHOTO_NAME_PATTERN,
                        Locale.getDefault()
                    ).format(Date())
                )
            )
        }

    override fun deletePhoto(uri: Uri) {
        fileStorage.deleteFile(uri)
    }
}
