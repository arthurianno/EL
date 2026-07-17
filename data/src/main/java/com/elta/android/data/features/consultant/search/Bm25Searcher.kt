package com.elta.android.data.features.consultant.search

import com.elta.android.domain.features.consultant.model.BotNode
import kotlin.math.ln

class Bm25Searcher(
    private val nodes: List<BotNode>,
    private val k1: Float = 1.5f,
    private val b: Float = 0.75f
) {
    private val documents: List<List<String>> = nodes.map { node ->
        // Документ для индексации состоит из самого вопроса бота и текста его кнопок-опций
        val allText = (node.text + " " + node.options.joinToString(" ") { it.text })
        tokenize(allText)
    }

    private val avgDocLength: Float
    private val docLengths: List<Int> = documents.map { it.size }
    private val docCount: Int = documents.size
    private val idf: Map<String, Float>
    private val termFrequencies: List<Map<String, Int>>

    init {
        avgDocLength = if (docLengths.isNotEmpty()) docLengths.average().toFloat() else 0f
        
        termFrequencies = documents.map { doc ->
            doc.groupingBy { it }.eachCount()
        }

        val docContainingTerm = mutableMapOf<String, Int>()
        for (doc in documents) {
            for (term in doc.distinct()) {
                docContainingTerm[term] = docContainingTerm.getOrDefault(term, 0) + 1
            }
        }

        idf = docContainingTerm.mapValues { (_, count) ->
            ln((docCount - count + 0.5f) / (count + 0.5f) + 1.0f)
        }
    }

    fun search(query: String, threshold: Float = 0.5f): BotNode? {
        val queryTerms = tokenize(query)
        if (queryTerms.isEmpty() || docCount == 0) return null

        var bestNode: BotNode? = null
        var maxScore = -1f

        for (i in 0 until docCount) {
            val docTerms = termFrequencies[i]
            val docLen = docLengths[i]
            var score = 0f

            for (term in queryTerms) {
                val tf = docTerms[term] ?: 0
                if (tf > 0) {
                    val idfVal = idf[term] ?: 0f
                    val numerator = tf * (k1 + 1f)
                    val denominator = tf + k1 * (1f - b + b * (docLen / avgDocLength))
                    score += idfVal * (numerator / denominator)
                }
            }

            if (score > maxScore) {
                maxScore = score
                bestNode = nodes[i]
            }
        }

        return if (maxScore >= threshold) bestNode else null
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-zA-Zа-яА-Я0-9 ]"), "")
            .split(" ")
            .filter { it.isNotBlank() && it.length > 2 } // Игнорируем союзы/предлоги короче 3 символов
            .map { stem(it) }
    }

    private fun stem(word: String): String {
        if (word.length <= 3) return word
        val endings = listOf("ами", "ями", "ов", "ей", "ам", "ям", "ом", "ем", "а", "я", "о", "е", "и", "ы", "у", "ю", "ь", "ть", "т")
        for (ending in endings) {
            if (word.endsWith(ending)) {
                return word.substring(0, word.length - ending.length)
            }
        }
        return word
    }
}
