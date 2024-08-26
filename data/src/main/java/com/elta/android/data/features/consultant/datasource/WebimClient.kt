package com.elta.android.data.features.consultant.datasource

import android.annotation.SuppressLint
import android.content.Context
import com.elta.android.common.di.qualifires.WebimAnnotation
import com.elta.android.common.di.qualifires.WebimAnnotationType
import com.elta.android.data.features.consultant.model.WebimChat
import com.elta.android.data.features.consultant.model.toDomain
import com.elta.android.data.features.consultant.model.toJsonObject
import com.elta.android.domain.features.consultant.model.ChatState
import com.elta.android.domain.features.consultant.model.ConnectionStatus
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
import java.util.UUID
import javax.inject.Inject

// todo: разделить на интерфейс возможно
@SuppressLint("CheckResult")
class WebimClient @Inject constructor(
    @WebimAnnotation(WebimAnnotationType.Account) private val accountName: String,
    @WebimAnnotation(WebimAnnotationType.Location) private val location: String,
    @WebimAnnotation(WebimAnnotationType.PrivateKey) private val privateKey: String,
    private val context: Context
) {
    private val _webimChatState: MutableStateFlow<ChatState> =
        MutableStateFlow(ChatState.Close)
    val webimChatState: StateFlow<ChatState>
        get() = _webimChatState.asStateFlow()

    private val _webimNetworkStatus: MutableStateFlow<ConnectionStatus> =
        MutableStateFlow(ConnectionStatus.Connecting)
    val webimNetworkStatus: StateFlow<ConnectionStatus>
        get() = _webimNetworkStatus.asStateFlow()

    private val _chat: MutableStateFlow<WebimChat> =
        MutableStateFlow(WebimChat.emptyChat)
    val chat: StateFlow<WebimChat>
        get() = _chat.asStateFlow()

    private var _tracker: MessageTracker? = null
    val tracker: MessageTracker
        get() = checkNotNull(_tracker)

    fun sessionCreate(webimUser: WebimUser, firebaseToken: String?): WebimSession =
        Webim.newSessionBuilder()
            .setPushSystem(Webim.PushSystem.FCM)
            .setPushToken(firebaseToken)
            .setAccountName(accountName)
            .setLocation(location)
            .setVisitorFieldsJson(webimUser.toJsonObject(privateKey))
            .setContext(context)
            .build()
            .setListeners()

    // избавить от деприкейтид
    private fun WebimSession.setListeners(): WebimSession {
        _tracker = stream.newMessageTracker(ChatMessages())
        return apply {
            stream.setGreetingMessageListener(ChatGreetingListener())
            stream.setChatStateListener { _, newState ->
                _webimChatState.value = newState.toDomain()
            }
            stream.setOnlineStatusChangeListener { _, newOnlineStatus ->
                _webimNetworkStatus.value = when (newOnlineStatus) {
                    MessageStream.OnlineStatus.UNKNOWN -> ConnectionStatus.Connecting
                    else -> {
                        stream.startChat()
                        ConnectionStatus.Online
                    }
                }
            }
        }
    }

    // внутренние классы вынести в какие-нибудь модели/сделать более читаемые
    private inner class ChatMessages : MessageListener {
        override fun messageAdded(before: Message?, message: Message) {
            val messages = chat.value.messages
                .toMutableList()
                .plus(message)

            emitNewChat(
                messages = messages,
                hasNewMessage = true
            )
        }

        override fun messageRemoved(message: Message) {
            val oldMessages = chat.value.messages.toMutableList()
            val indexMessage =
                oldMessages.indexOf(oldMessages.first { it.clientSideId == message.clientSideId })
            emitNewChat(
                chat.value.messages.toMutableList()
                    .apply {
                        removeAt(indexMessage)
                    }
            )
        }

        override fun messageChanged(from: Message, to: Message) {
            // нет обработки ошибки
            runCatching {
                val oldMessages = chat.value.messages.toMutableList()
                val indexMessage =
                    oldMessages.indexOf(oldMessages.first { it.serverSideId == from.serverSideId })
                emitNewChat(
                    oldMessages
                        .apply {
                            removeAt(indexMessage)
                            add(indexMessage, to)
                        }
                )
            }
        }

        override fun allMessagesRemoved() {
            _chat.tryEmit(WebimChat.emptyChat)
        }
    }

    private fun emitNewChat(
        messages: List<Message>,
        hasNewMessage: Boolean = false
    ) {
        _chat.tryEmit(
            chat.value.copy(
                id = UUID.randomUUID().toString(),
                messages = messages,
                hasNewMessage = hasNewMessage
            )
        )

    }

    private inner class ChatGreetingListener : MessageStream.GreetingMessageListener {
        override fun greetingMessage(message: String) {

        }
    }
}
