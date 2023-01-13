package com.elta.android.data.features.consultant.datasource

import android.annotation.SuppressLint
import android.content.Context
import com.elta.android.common.di.qualifires.WebimAnnotation
import com.elta.android.common.di.qualifires.WebimAnnotationType
import com.elta.android.data.features.consultant.model.toDomain
import com.elta.android.data.features.consultant.model.toJsonObject
import com.elta.android.domain.features.consultant.model.ChatList
import com.elta.android.domain.features.consultant.model.WebimChatState
import com.elta.android.domain.features.consultant.model.WebimMessage
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.domain.features.consultant.model.WebimMessageType
import com.elta.android.domain.features.consultant.model.WebimOwner
import com.elta.android.domain.features.consultant.model.WebimStatus
import com.elta.android.domain.features.consultant.model.WebimUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.webim.android.sdk.Message
import ru.webim.android.sdk.MessageListener
import ru.webim.android.sdk.MessageStream
import ru.webim.android.sdk.MessageTracker
import ru.webim.android.sdk.Webim
import ru.webim.android.sdk.WebimSession
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@SuppressLint("CheckResult")
class WebimDataSource @Inject constructor(
    @WebimAnnotation(WebimAnnotationType.Account) private val accountName: String,
    @WebimAnnotation(WebimAnnotationType.Location) private val location: String,
    @WebimAnnotation(WebimAnnotationType.PrivateKey) private val privateKey: String,
    private val context: Context
) {
    private val _webimChatState: MutableStateFlow<WebimChatState> =
        MutableStateFlow(WebimChatState.Close)
    val webimChatState: StateFlow<WebimChatState>
        get() = _webimChatState.asStateFlow()

    private val _webimNetworkStatus: MutableStateFlow<WebimStatus> =
        MutableStateFlow(WebimStatus.Connecting)
    val webimNetworkStatus: StateFlow<WebimStatus>
        get() = _webimNetworkStatus.asStateFlow()

    private val _chat: MutableStateFlow<ChatList> =
        MutableStateFlow(ChatList.emptyChat)
    val chat: StateFlow<ChatList>
        get() = _chat.asStateFlow()

    private var _tracker: MessageTracker? = null
    val tracker: MessageTracker
        get() = checkNotNull(_tracker)

    fun sessionCreate(webimUser: WebimUser): WebimSession =
        Webim.newSessionBuilder()
            .setAccountName(accountName)
            .setLocation(location)
            .setVisitorFieldsJson(webimUser.toJsonObject(privateKey))
            .setContext(context)
            .build()
            .setListeners()

    private fun WebimSession.setListeners(): WebimSession {
        _tracker = stream.newMessageTracker(ChatMessages())
        return apply {
            stream.setGreetingMessageListener(ChatGreetingListener())
            stream.setChatStateListener { _, newState ->
                _webimChatState.value = when (newState) {
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
                _webimNetworkStatus.value = when (newOnlineStatus) {
                    MessageStream.OnlineStatus.ONLINE,
                    MessageStream.OnlineStatus.BUSY_ONLINE -> {
                        stream.startChat()
                        WebimStatus.Online
                    }

                    MessageStream.OnlineStatus.UNKNOWN -> WebimStatus.Connecting

                    else -> WebimStatus.Offline
                }
            }
        }
    }

    private inner class ChatMessages : MessageListener {
        override fun messageAdded(before: Message?, message: Message) {
            emitNewChat(
                chat.value.messages
                    .toMutableList()
                    .apply {
                        add(message.toDomain())
                    },
                hasNewMessages = true
            )
        }

        override fun messageRemoved(message: Message) {
            emitNewChat(
                _chat.value.messages
                    .toMutableList()
                    .apply {
                        remove(message.toDomain())
                    }
            )
        }

        override fun messageChanged(from: Message, to: Message) {
            val oldMessages = _chat.value.messages.toMutableList()
            val indexMessage =
                oldMessages.indexOf(oldMessages.first { it.id == from.toDomain().id })
            emitNewChat(
                oldMessages
                    .apply {
                        removeAt(indexMessage)
                        add(indexMessage, to.toDomain())
                    }
            )
        }

        override fun allMessagesRemoved() {
            _chat.tryEmit(ChatList.emptyChat)
        }
    }

    private fun emitNewChat(
        newList: MutableList<WebimMessage>,
        hasNewMessages: Boolean = false
    ) {
        _chat.tryEmit(chat.value.copy(messages = newList, hasNewMessage = hasNewMessages))
    }

    private inner class ChatGreetingListener : MessageStream.GreetingMessageListener {
        override fun greetingMessage(message: String) {
            emitNewChat(
                _chat.value.messages
                    .toMutableList()
                    .apply {
                        add(
                            WebimMessage(
                                id = UUID.randomUUID().toString(),
                                owner = WebimOwner.Operator,
                                type = WebimMessageType.Text,
                                text = message,
                                time = Date().time,
                                sendStatus = WebimMessageSendStatus.Sent,
                                isRead = true,
                                attachment = null
                            )
                        )
                    },
                hasNewMessages = true
            )
        }
    }
}
