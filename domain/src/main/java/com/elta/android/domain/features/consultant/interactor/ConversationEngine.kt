package com.elta.android.domain.features.consultant.interactor

import com.elta.android.domain.features.consultant.model.BotNode
import com.elta.android.domain.features.consultant.model.BotOption
import java.util.Stack

class ConversationEngine {
    private val historyStack = Stack<BotNode>()
    private var currentNode: BotNode? = null

    fun reset(rootNode: BotNode) {
        historyStack.clear()
        currentNode = rootNode
    }

    fun getCurrentNode(): BotNode? = currentNode

    fun getHistory(): List<BotNode> = historyStack.toList()

    suspend fun selectOption(option: BotOption, getNodeById: suspend (String) -> BotNode?): BotNode? {
        val current = currentNode ?: return null
        val nextNode = getNodeById(option.nextNodeId) ?: return null
        historyStack.push(current)
        currentNode = nextNode
        return nextNode
    }

    fun goBack(): BotNode? {
        if (historyStack.isEmpty()) return null
        val previousNode = historyStack.pop()
        currentNode = previousNode
        return previousNode
    }

    fun canGoBack(): Boolean = historyStack.isNotEmpty()
}
