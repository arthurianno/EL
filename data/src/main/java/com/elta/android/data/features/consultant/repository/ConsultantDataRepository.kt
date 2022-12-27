package com.elta.android.data.features.consultant.repository

import android.content.Context
import com.elta.android.data.features.consultant.model.toDomain
import com.elta.android.data.features.consultant.model.toJSonObject
import com.elta.android.data.features.consultant.model.toWebimUser
import com.elta.android.domain.features.consultant.model.WebimChatState
import com.elta.android.domain.features.consultant.model.WebimMessage
import com.elta.android.domain.features.consultant.model.WebimMessageType
import com.elta.android.domain.features.consultant.model.WebimOwner
import com.elta.android.domain.features.consultant.model.WebimStatus
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import com.elta.android.domain.features.user.repository.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.webim.android.sdk.Message
import ru.webim.android.sdk.MessageListener
import ru.webim.android.sdk.MessageStream
import ru.webim.android.sdk.MessageStream.GreetingMessageListener
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
    private val _messages: MutableStateFlow<List<WebimMessage>> =
        MutableStateFlow(emptyList())
    override val messages: StateFlow<List<WebimMessage>>
        get() = _messages.asStateFlow()

    private var _webimSession: WebimSession? = null
        set(value) {
            tracker = value?.stream?.newMessageTracker(ChatMessages())
            field = value?.apply {
                stream.setGreetingMessageListener(ChatGreetingListener())
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
                        MessageStream.OnlineStatus.BUSY_ONLINE -> {
                            _webimSession?.stream?.startChat()
                            WebimStatus.Online
                        }

                        MessageStream.OnlineStatus.UNKNOWN -> WebimStatus.Connecting

                        else -> WebimStatus.Offline
                    }
                }
            }
        }

    private var tracker: MessageTracker? = null
    private val webimSession: WebimSession
        get() = checkNotNull(_webimSession)

    init {
        profileRepository.getProfile()
            .map { it.toWebimUser() }
            .subscribe({
                user = it
            }, {
                user = null
            })
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

    private inner class ChatMessages : MessageListener {
        override fun messageAdded(before: Message?, message: Message) {
            _messages.tryEmit(
                _messages.value
                    .toMutableList()
                    .apply {
                        add(message.toDomain())
                    }
            )
        }

        override fun messageRemoved(message: Message) {
            _messages.tryEmit(
                _messages.value
                    .toMutableList()
                    .apply {
                        remove(message.toDomain())
                    }
            )
        }

        override fun messageChanged(from: Message, to: Message) {
            val oldMessages = _messages.value.toMutableList()
            val indexMessage = oldMessages.indexOf(from.toDomain())
            _messages.tryEmit(
                oldMessages
                    .apply {
                        removeAt(indexMessage)
                        add(indexMessage, to.toDomain())
                    }
            )
        }

        override fun allMessagesRemoved() {
            _messages.tryEmit(emptyList())
        }
    }

    private inner class ChatGreetingListener : GreetingMessageListener {
        override fun greetingMessage(message: String) {
            _messages.tryEmit(
                _messages.value
                    .toMutableList()
                    .apply {
                        add(
                            WebimMessage(
                                WebimOwner.Operator,
                                WebimMessageType.Text,
                                content = message
                            )
                        )
                    }
            )
        }
    }
}
