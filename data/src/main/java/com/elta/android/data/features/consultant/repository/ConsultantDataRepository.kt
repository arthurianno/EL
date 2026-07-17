package com.elta.android.data.features.consultant.repository

import android.content.Context
import com.elta.android.data.features.common.cache.BoxScope
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.consultant.cache.ChatMessageDbEntity
import com.elta.android.data.features.consultant.cache.UserStateDbEntity
import com.elta.android.data.features.consultant.search.Bm25Searcher
import com.elta.android.data.features.user.dto.SupportedLanguageTag
import com.elta.android.domain.features.consultant.model.BotNode
import com.elta.android.domain.features.consultant.model.BotOption
import com.elta.android.domain.features.consultant.model.ChatMessage
import com.elta.android.domain.features.consultant.model.UserState
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import com.google.gson.Gson
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsultantDataRepository @Inject constructor(
    private val context: Context,
    private val factory: BoxStoreFactory,
    override val dispatcher: CoroutineDispatcher
) : ConsultantRepository {

    private var loadedLanguage: String? = null
    private var faqTree: FaqTree? = null
    private var bm25Searcher: Bm25Searcher? = null

    private val messageBox: Box<ChatMessageDbEntity> by lazy {
        factory.getBoxStore(BoxScope.PER_USER).boxFor(ChatMessageDbEntity::class)
    }

    private val userStateBox: Box<UserStateDbEntity> by lazy {
        factory.getBoxStore(BoxScope.PER_USER).boxFor(UserStateDbEntity::class)
    }

    private val messagesStateFlow = MutableStateFlow<List<ChatMessage>>(emptyList())

    private fun getLanguageTag(): String {
        val prefs = context.getSharedPreferences("language_preference", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("selected_language", null)
        val rawLanguage = savedLanguage ?: java.util.Locale.getDefault().language
        return SupportedLanguageTag.fromRawValue(rawLanguage).value
    }

    private suspend fun getOrLoadFaqTree(): FaqTree = withContext(dispatcher) {
        val currentLang = getLanguageTag()
        if (faqTree == null || loadedLanguage != currentLang) {
            loadedLanguage = currentLang
            faqTree = loadFaqTreeFromAssets(currentLang)
            bm25Searcher = null
        }
        faqTree!!
    }

    private fun loadFaqTreeFromAssets(lang: String): FaqTree {
        return try {
            val fileName = if (lang == "en") "faq_tree_en.json" else "faq_tree.json"
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val dto = Gson().fromJson(jsonString, FaqTreeDto::class.java)
            
            val domainNodes = dto.nodes.map { nodeDto ->
                BotNode(
                    id = nodeDto.id,
                    text = nodeDto.text,
                    options = nodeDto.options.map { optionDto ->
                        BotOption(
                            text = optionDto.text,
                            nextNodeId = optionDto.nextNodeId,
                            analyticsEventName = optionDto.analyticsEventName
                        )
                    }
                )
            }
            FaqTree(
                rootNodeId = dto.rootNodeId,
                nodes = domainNodes.associateBy { it.id }
            )
        } catch (e: Exception) {
            // Фолбэк на пустую структуру при ошибке чтения
            FaqTree(rootNodeId = "root", nodes = emptyMap())
        }
    }

    override suspend fun getRootNode(): BotNode = withContext(dispatcher) {
        val tree = getOrLoadFaqTree()
        val defaultText = if (getLanguageTag() == "en") {
            "Failed to load FAQ-bot. Please try again later."
        } else {
            "Ошибка загрузки FAQ-бота. Пожалуйста, попробуйте позже."
        }
        tree.nodes[tree.rootNodeId] ?: BotNode(
            id = "root",
            text = defaultText,
            options = emptyList()
        )
    }

    override suspend fun getNodeById(id: String): BotNode? = withContext(dispatcher) {
        val tree = getOrLoadFaqTree()
        tree.nodes[id]
    }

    override suspend fun searchNodeByText(query: String): BotNode? = withContext(dispatcher) {
        val tree = getOrLoadFaqTree()
        var searcher = bm25Searcher
        if (searcher == null) {
            searcher = Bm25Searcher(tree.nodes.values.toList())
            bm25Searcher = searcher
        }
        searcher.search(query)
    }

    override fun getMessagesFlow(): Flow<List<ChatMessage>> {
        val cached = messageBox.all
            .map { it.toDomain() }
            .sortedBy { it.timestamp }
        messagesStateFlow.value = cached
        return messagesStateFlow.asStateFlow()
    }

    override suspend fun saveMessage(message: ChatMessage) = withContext(dispatcher) {
        messageBox.put(ChatMessageDbEntity.fromDomain(message))
        val currentList = messagesStateFlow.value.toMutableList()
        currentList.add(message)
        messagesStateFlow.value = currentList
        Unit
    }

    override suspend fun clearHistory() = withContext(dispatcher) {
        messageBox.removeAll()
        messagesStateFlow.value = emptyList()
    }

    override suspend fun getBotState(): UserState = withContext(dispatcher) {
        val entity = userStateBox.get(UserStateDbEntity.SINGLETON_ID)
        entity?.toDomain() ?: UserState()
    }

    override suspend fun saveBotState(state: UserState) = withContext(dispatcher) {
        userStateBox.put(UserStateDbEntity.fromDomain(state))
        Unit
    }

    override suspend fun clearBotState() = withContext(dispatcher) {
        userStateBox.remove(UserStateDbEntity.SINGLETON_ID)
        Unit
    }

    private data class FaqTree(
        val rootNodeId: String,
        val nodes: Map<String, BotNode>
    )

    private data class FaqTreeDto(
        val rootNodeId: String,
        val nodes: List<BotNodeDto>
    )

    private data class BotNodeDto(
        val id: String,
        val text: String,
        val options: List<BotOptionDto>
    )

    private data class BotOptionDto(
        val text: String,
        val nextNodeId: String,
        val analyticsEventName: String? = null
    )
}
