package com.elta.android.data.features.consultant.repository

import com.elta.android.data.features.consultant.datasource.WebimDataSource
import com.elta.android.domain.features.consultant.model.ChatList
import com.elta.android.domain.features.consultant.model.WebimChatState
import com.elta.android.domain.features.consultant.model.WebimStatus
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import ru.webim.android.sdk.WebimSession
import javax.inject.Inject

class ConsultantDataRepository @Inject constructor(
    private val webimDataSource: WebimDataSource,
    override val dispatcher: CoroutineDispatcher
) : ConsultantRepository {

    private var _webimSession: WebimSession? = null
    private val webimSession
        get() = requireNotNull(_webimSession)

    override fun webimSessionCreate(webimUser: WebimUser) {
        _webimSession = webimDataSource.sessionCreate(webimUser)
    }

    override fun webimResume() {
        webimSession.resume()
    }

    override fun webimPause() {
        webimSession.pause()
    }

    override fun webimDestroy() {
        webimSession.destroy()
    }

    override fun startChat() {
        webimSession.stream.startChat()
    }

    override suspend fun sendMessage(message: String) {
        webimSession.stream.sendMessage(message)
    }

    override fun chatState(): Flow<WebimChatState> =
        webimDataSource.webimChatState

    override fun chatNetworkStatus(): Flow<WebimStatus> =
        webimDataSource.webimNetworkStatus

    override val chat: Flow<ChatList> =
        webimDataSource.chat
}
