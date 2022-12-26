package com.elta.android.data.features.consultant.repository

import android.content.Context
import com.elta.android.data.features.consultant.model.toJSonObject
import com.elta.android.data.features.consultant.model.toWebimUser
import com.elta.android.domain.features.consultant.model.WebimChatState
import com.elta.android.domain.features.consultant.model.WebimMessage
import com.elta.android.domain.features.consultant.model.WebimStatus
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import com.elta.android.domain.features.user.repository.ProfileRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import ru.webim.android.sdk.Message
import ru.webim.android.sdk.MessageListener
import ru.webim.android.sdk.MessageStream
import ru.webim.android.sdk.MessageTracker
import ru.webim.android.sdk.Webim
import ru.webim.android.sdk.WebimSession
import javax.inject.Inject

private const val ACCOUNT_NAME = "wwwmarslabru"
private const val LOCATION_NAME = "mobile"
private const val PRIVATE_KEY = "8599c5abfcd7342b5feac6599279ca06"

class ConsultantDataRepository @Inject constructor(
    private val context: Context,
    private val profileRepository: ProfileRepository,
    override val dispatcher: CoroutineDispatcher
) : ConsultantRepository {
    private val webimChatState: MutableStateFlow<WebimChatState> =
        MutableStateFlow(WebimChatState.Close)

    private val webimNetworkStatus: MutableStateFlow<WebimStatus> =
        MutableStateFlow(WebimStatus.Connecting)
    private var user: WebimUser? = null
    private var _webimSession: WebimSession? = null
        set(value) {
            _tracker = value?.stream?.newMessageTracker(ChatMessages())
            field = value?.apply {
                stream.setChatStateListener { _, newState ->
                    webimChatState.value = when (newState) {
                        MessageStream.ChatState.CHATTING -> WebimChatState.Chatting
                        MessageStream.ChatState.CHATTING_WITH_ROBOT -> WebimChatState.Chatting
                        MessageStream.ChatState.CLOSED_BY_OPERATOR -> WebimChatState.Close
                        MessageStream.ChatState.CLOSED_BY_VISITOR -> WebimChatState.Close
                        MessageStream.ChatState.DELETED -> WebimChatState.Close
                        MessageStream.ChatState.INVITATION -> WebimChatState.Open
                        MessageStream.ChatState.ROUTING -> WebimChatState.Open
                        MessageStream.ChatState.NONE -> WebimChatState.Close
                        MessageStream.ChatState.QUEUE -> WebimChatState.Open
                        MessageStream.ChatState.UNKNOWN -> WebimChatState.Close
                    }
                }
                stream.setOnlineStatusChangeListener { _, newOnlineStatus ->
                    webimNetworkStatus.value = when (newOnlineStatus) {
                        MessageStream.OnlineStatus.ONLINE,
                        MessageStream.OnlineStatus.BUSY_ONLINE -> WebimStatus.Online

                        MessageStream.OnlineStatus.UNKNOWN -> WebimStatus.Connecting

                        else -> WebimStatus.Offline
                    }
                }
            }
        }
    private var _tracker: MessageTracker? = null
    private val tracker: MessageTracker
        get() = checkNotNull(_tracker)

    private val webimSession: WebimSession
        get() = checkNotNull(_webimSession)
    private val disposableContainer = CompositeDisposable()

    init {
        disposableContainer.add(
            profileRepository.getProfile()
                .map { it.toWebimUser() }
                .subscribe({
                    user = it
                }, {
                    user = null
                })
        )
    }

    override fun webimSessionCreate() {
        _webimSession = Webim.newSessionBuilder()
            .setAccountName(ACCOUNT_NAME)
            .setLocation(LOCATION_NAME)
            .also {
                user?.let { user ->
                    val toJSonObject = user.toJSonObject(PRIVATE_KEY)
                    it.setVisitorFieldsJson(toJSonObject)
                }
            }
            .setContext(context)
            .build()
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
        webimChatState.asStateFlow()

    override fun chatNetworkStatus(): Flow<WebimStatus> =
        webimNetworkStatus.asStateFlow()

    override fun messages(): Flow<List<WebimMessage>> = flow {
        tracker.getAllMessages {
        }
    }

    private inner class ChatMessages : MessageListener {
        override fun messageAdded(before: Message?, message: Message) {
            TODO("Not yet implemented")
        }

        override fun messageRemoved(message: Message) {
            TODO("Not yet implemented")
        }

        override fun messageChanged(from: Message, to: Message) {
            TODO("Not yet implemented")
        }

        override fun allMessagesRemoved() {
            TODO("Not yet implemented")
        }
    }
}
