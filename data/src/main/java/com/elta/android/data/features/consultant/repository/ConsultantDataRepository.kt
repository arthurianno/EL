package com.elta.android.data.features.consultant.repository

import android.content.Context
import com.elta.android.data.features.user.dto.SupportedLanguageTag
import com.elta.android.domain.features.consultant.model.BotNode
import com.elta.android.domain.features.consultant.model.BotOption
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsultantDataRepository @Inject constructor(
    private val context: Context,
    override val dispatcher: CoroutineDispatcher
) : ConsultantRepository {

    private var loadedLanguage: String? = null
    private var faqTree: FaqTree? = null

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
