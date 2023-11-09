package com.elta.android.data.core.paging

import androidx.paging.PagingSource
import com.elta.android.domain.features.calculator.model.Dish

abstract class BasePagingSource {

    abstract val defaultPosition: Int
    abstract val pagingSource: PagingSource<Int, Dish>
    abstract fun setQuery(vararg query: Any)

    fun <T : Any> returnResult(
        data: List<T>,
        currentPage: Int,
        totalPage: Int,
        pageSize: Int,
    ): PagingSource.LoadResult<Int, T> {

        val prevKey = getPrevKey(currentPage)
        val nextKey = getNextKey(currentPage, pageSize, totalPage)

        return PagingSource.LoadResult.Page(
            data = data, prevKey = prevKey, nextKey = nextKey
        )
    }

    private fun getNextKey(currentPage: Int, pageSize: Int, totalPage: Int) =
        if (countResults(currentPage, pageSize) <= totalPage)
            currentPage + DEFAULT_PAGING_STEP
        else
            null

    private fun getPrevKey(currentPage: Int) =
        if (currentPage != defaultPosition)
            currentPage - DEFAULT_PAGING_STEP
        else null

    private fun countResults(currentPage: Int, pageSize: Int) =
        ((currentPage + DEFAULT_PAGING_STEP) - defaultPosition) * pageSize
}

const val DEFAULT_PAGING_STEP = 1
