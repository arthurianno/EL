package com.elta.android.data.features.calculator.datasource

import com.elta.android.data.features.calculator.cache.dto.SearchHistoryDto
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import javax.inject.Inject

class CalculatorCacheDataSource @Inject constructor(
    private val searchWordHistory: Cache<SearchHistoryDto>
) {

    fun getHistoryWords(): Flow<List<String>> = flowOf(
        searchWordHistory.getAll(CommonConditions.All)
            .sortedByDescending { it.time }
            .map { it.word }
    )

    fun saveWordToHistory(word: String): Flow<Unit> = flow {
        emit(
            searchWordHistory.update(
                listOf(
                    SearchHistoryDto(
                        id = word.hashCode().toLong(),
                        word = word,
                        time = Date().time
                    )
                )
            )
        )
    }
}
