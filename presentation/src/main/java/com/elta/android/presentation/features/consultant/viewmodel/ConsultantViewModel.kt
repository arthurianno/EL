package com.elta.android.presentation.features.consultant.viewmodel

import android.content.Context
import com.elta.android.domain.common.usecase.CopyTextUseCase
import com.elta.android.domain.features.consultant.interactor.ConversationEngine
import com.elta.android.domain.features.consultant.model.BotNode
import com.elta.android.domain.features.consultant.model.BotOption
import com.elta.android.domain.features.consultant.model.ChatMessage
import com.elta.android.domain.features.consultant.model.MessageSender
import com.elta.android.domain.features.consultant.model.MessageStatus
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
        startConversation()
    }

    private fun startConversation() {
        launch {
            val rootNode = botRepository.getRootNode()
            conversationEngine.reset(rootNode)
            
            // Сбрасываем стейт и показываем приветствие
            reduceState {
                createInitState()
            }
            
            showBotNodeMessage(rootNode, initialDelay = true)
        }
    }

    private suspend fun showBotNodeMessage(node: BotNode, initialDelay: Boolean) {
        // Устанавливаем статус печатания
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
            // Вычисляем задержку на основе длины текста (например, 10 мс за символ, но не меньше 500 мс и не больше 1500 мс)
            val typingDuration = (node.text.length * 10L).coerceIn(500L, 1500L)
            delay(typingDuration)
        }

        val botMessage = ChatMessage(
            text = node.text,
            sender = MessageSender.BOT
        )

        reduceState {
            state.value.copy(
                chatMessages = state.value.chatMessages + botMessage,
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
            is ConsultantAction.ResetClick -> startConversation()
            is ConsultantAction.CopyMessageClick -> handleCopyMessage(action.text)
        }
    }

    private fun handleOptionClick(option: BotOption) {
        launch {
            // Добавляем сообщение пользователя на экран
            val userMessage = ChatMessage(
                text = option.text,
                sender = MessageSender.USER
            )

            reduceState {
                state.value.copy(
                    chatMessages = state.value.chatMessages + userMessage,
                    currentOptions = emptyList() // Временно гасим кнопочки
                )
            }
            sendEvent(ScrollToDown)

            // Запрашиваем следующий узел
            val nextNode = conversationEngine.selectOption(option) { nodeId ->
                botRepository.getNodeById(nodeId)
            }

            if (nextNode != null) {
                showBotNodeMessage(nextNode, initialDelay = false)
            } else {
                // Если узел не найден, завершаем или откатываем
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
            
            // Удаляем из списка сообщений последнее сообщение пользователя и последний ответ бота
            val currentList = state.value.chatMessages
            val updatedList = if (currentList.size >= 2) {
                currentList.dropLast(2)
            } else {
                emptyList()
            }

            reduceState {
                state.value.copy(
                    chatMessages = updatedList,
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
