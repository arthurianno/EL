package com.elta.android.presentation.features.consultant.viewmodel

import android.content.Context
import com.elta.android.domain.common.usecase.CopyTextUseCase
import com.elta.android.domain.features.consultant.interactor.ConversationEngine
import com.elta.android.domain.features.consultant.model.BotNode
import com.elta.android.domain.features.consultant.model.BotOption
import com.elta.android.domain.features.consultant.model.ChatMessage
import com.elta.android.domain.features.consultant.model.MessageSender
import com.elta.android.domain.features.consultant.model.MessageStatus
import com.elta.android.domain.features.consultant.model.UserState
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import com.elta.android.presentation.R
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.features.consultant.model.ConsultantViewState
import com.elta.android.presentation.features.consultant.model.ScrollToDown
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

class ConsultantViewModel @Inject constructor(
    private val botRepository: ConsultantRepository,
    private val copyText: CopyTextUseCase,
    private val appMetricTracker: AppMetricTracker,
    private val context: Context
) : BaseViewModel<ConsultantViewState>() {

    private val conversationEngine = ConversationEngine()

    override fun createInitState(): ConsultantViewState =
        ConsultantViewState(
            chatMessages = emptyList(),
            currentOptions = emptyList(),
            isBotTyping = false,
            canGoBack = false
        )

    init {
        appMetricTracker.trackEvent(AppMetricEvent.TapOnlineConsultant)
        observeMessages()
        restoreOrCreateConversation()
    }

    private fun observeMessages() {
        launch {
            botRepository.getMessagesFlow().collect { messages ->
                reduceState {
                    state.value.copy(
                        chatMessages = messages
                    )
                }
                sendEvent(ScrollToDown)
            }
        }
    }

    private fun restoreOrCreateConversation() {
        launch {
            val savedState = botRepository.getBotState()
            val rootNode = botRepository.getRootNode()

            val savedNodeId = savedState.currentNodeId
            if (savedNodeId != null) {
                val currentNode = botRepository.getNodeById(savedNodeId) ?: rootNode
                val historyNodes = savedState.historyStack.mapNotNull { botRepository.getNodeById(it) }
                
                conversationEngine.restoreState(currentNode, historyNodes)

                reduceState {
                    state.value.copy(
                        currentOptions = currentNode.options,
                        canGoBack = conversationEngine.canGoBack()
                    )
                }
            } else {
                resetConversation()
            }
        }
    }

    private fun resetConversation() {
        launch {
            val rootNode = botRepository.getRootNode()
            conversationEngine.reset(rootNode)
            botRepository.clearHistory()
            botRepository.clearBotState()
            
            showBotNodeMessage(rootNode, initialDelay = true)
        }
    }

    private suspend fun showBotNodeMessage(node: BotNode, initialDelay: Boolean) {
        reduceState {
            state.value.copy(
                isBotTyping = true,
                canGoBack = conversationEngine.canGoBack()
            )
        }
        sendEvent(ScrollToDown)

        if (initialDelay) {
            delay(800)
        } else {
            val typingDuration = (node.text.length * 10L).coerceIn(500L, 1500L)
            delay(typingDuration)
        }

        val botMessage = ChatMessage(
            text = node.text,
            sender = MessageSender.BOT
        )

        botRepository.saveMessage(botMessage)
        botRepository.saveBotState(
            UserState(
                currentScenarioId = "local_faq",
                currentNodeId = node.id,
                historyStack = conversationEngine.getHistory().map { it.id }
            )
        )

        reduceState {
            state.value.copy(
                currentOptions = node.options,
                isBotTyping = false,
                canGoBack = conversationEngine.canGoBack()
            )
        }
        sendEvent(ScrollToDown)
    }

    override fun handleUserAction(action: Action) {
        super.handleUserAction(action)
        when (action) {
            is ConsultantAction.OptionClick -> handleOptionClick(action.option)
            is ConsultantAction.BackClick -> handleBackClick()
            is ConsultantAction.ResetClick -> resetConversation()
            is ConsultantAction.CopyMessageClick -> handleCopyMessage(action.text)
            is ConsultantAction.SendTextClick -> handleSendTextClick(action.text)
        }
    }

    private fun handleSendTextClick(text: String) {
        if (text.isBlank()) return
        launch {
            val userMessage = ChatMessage(
                text = text,
                sender = MessageSender.USER
            )
            botRepository.saveMessage(userMessage)

            reduceState {
                state.value.copy(
                    currentOptions = emptyList()
                )
            }
            sendEvent(ScrollToDown)

            reduceState {
                state.value.copy(isBotTyping = true)
            }
            sendEvent(ScrollToDown)

            delay(1000)

            val matchedNode = botRepository.searchNodeByText(text)

            if (matchedNode != null) {
                conversationEngine.reset(matchedNode)
                showBotNodeMessage(matchedNode, initialDelay = false)
            } else {
                val isEn = context.resources.configuration.locale.language == "en"
                val fallbackText = if (isEn) {
                    "Sorry, I couldn't find an answer to your question. Please try asking differently."
                } else {
                    "К сожалению, я не смог подобрать решение. Попробуйте сформулировать вопрос иначе."
                }

                val botMessage = ChatMessage(
                    text = fallbackText,
                    sender = MessageSender.BOT
                )
                botRepository.saveMessage(botMessage)

                botRepository.saveBotState(
                    UserState(
                        currentScenarioId = "local_faq",
                        currentNodeId = null,
                        historyStack = emptyList()
                    )
                )

                reduceState {
                    state.value.copy(
                        currentOptions = listOf(
                            BotOption(context.getString(R.string.consultant_to_start), "root")
                        ),
                        isBotTyping = false,
                        canGoBack = conversationEngine.canGoBack()
                    )
                }
                sendEvent(ScrollToDown)
            }
        }
    }

    private fun handleOptionClick(option: BotOption) {
        launch {
            val userMessage = ChatMessage(
                text = option.text,
                sender = MessageSender.USER
            )
            botRepository.saveMessage(userMessage)

            reduceState {
                state.value.copy(
                    currentOptions = emptyList()
                )
            }
            sendEvent(ScrollToDown)

            val nextNode = conversationEngine.selectOption(option) { nodeId ->
                botRepository.getNodeById(nodeId)
            }

            if (nextNode != null) {
                showBotNodeMessage(nextNode, initialDelay = false)
            } else {
                reduceState {
                    state.value.copy(
                        currentOptions = listOf(BotOption(context.getString(R.string.consultant_to_start), "root"))
                    )
                }
            }
        }
    }

    private fun handleBackClick() {
        if (!conversationEngine.canGoBack()) return

        launch {
            val previousNode = conversationEngine.goBack() ?: return@launch
            
            botRepository.saveBotState(
                UserState(
                    currentScenarioId = "local_faq",
                    currentNodeId = previousNode.id,
                    historyStack = conversationEngine.getHistory().map { it.id }
                )
            )

            val currentList = state.value.chatMessages
            botRepository.clearHistory()
            if (currentList.size >= 2) {
                currentList.dropLast(2).forEach {
                    botRepository.saveMessage(it)
                }
            }

            reduceState {
                state.value.copy(
                    currentOptions = previousNode.options,
                    canGoBack = conversationEngine.canGoBack()
                )
            }
            sendEvent(ScrollToDown)
        }
    }

    private fun handleCopyMessage(text: String) {
        copyText(text)
    }

    override fun backClick() {
        if (conversationEngine.canGoBack()) {
            handleBackClick()
        } else {
            super.backClick()
        }
    }
}
