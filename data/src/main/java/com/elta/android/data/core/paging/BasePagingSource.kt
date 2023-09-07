package com.elta.android.data.core.paging

import androidx.paging.PagingSource
import com.elta.android.domain.features.calculator.model.Dish

interface BasePagingSource{
    val pagingSource: PagingSource<Int, Dish>
    fun setQuery(query: String)
}

const val DEFAULT_POSITION = 0
const val DEFAULT_PAGING_STEP = 1
