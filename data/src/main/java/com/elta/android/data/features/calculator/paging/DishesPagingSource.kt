package com.elta.android.data.features.calculator.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.elta.android.data.core.paging.BasePagingSource
import com.elta.android.data.features.calculator.datasource.fatsecret.FatSecretDataSource
import com.elta.android.data.features.calculator.mapper.compactFoodsToDomain
import com.elta.android.domain.features.calculator.model.Dish
import javax.inject.Inject

class DishesPagingSource @Inject constructor(
    private val fatSecretDataSource: FatSecretDataSource,
) : BasePagingSource() {

    override val defaultPosition: Int = DEFAULT_POSITION

    private var name = ""
    override fun setQuery(vararg query: Any) {
        name = query[0] as String
    }

    override val pagingSource: PagingSource<Int, Dish> = object : PagingSource<Int, Dish>() {

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Dish> {

            val currentPage = params.key ?: DEFAULT_POSITION
            val pageSize = params.loadSize

            return try {
                val foodsSearch =
                    fatSecretDataSource.searchDishes(name, currentPage, pageSize).foodsSearch
                val dishes = foodsSearch.results?.food?.compactFoodsToDomain() ?: emptyList()
                val totalPage = foodsSearch.totalResults.toIntOrNull() ?: dishes.size


                returnResult(
                    dishes,
                    currentPage,
                    totalPage,
                    pageSize
                )
            } catch (ex: Exception) {
                LoadResult.Error(ex)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, Dish>): Int? {
            return state.anchorPosition
        }
    }

}

private const val DEFAULT_POSITION = 0
