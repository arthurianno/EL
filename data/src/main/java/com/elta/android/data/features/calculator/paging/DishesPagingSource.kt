package com.elta.android.data.features.calculator.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.elta.android.data.core.paging.BasePagingSource
import com.elta.android.data.core.paging.DEFAULT_PAGING_STEP
import com.elta.android.data.core.paging.DEFAULT_POSITION
import com.elta.android.data.features.calculator.datasource.fatsecret.FatSecretDataSource
import com.elta.android.data.features.calculator.mapper.compactFoodsToDomain
import com.elta.android.domain.features.calculator.model.Dish
import javax.inject.Inject

class DishesPagingSource @Inject constructor(
    private val fatSecretDataSource: FatSecretDataSource,
) : BasePagingSource {

    private var name = ""
    override fun setQuery(query: String) {
        name = query
    }

    override val pagingSource: PagingSource<Int, Dish> = object : PagingSource<Int, Dish>() {

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Dish> {

            val currentPage = params.key ?: DEFAULT_POSITION
            val pageSize = params.loadSize

            return try {
                val foodsSearch = fatSecretDataSource.searchDishes(name, currentPage, pageSize).foodsSearch
                val dishes = foodsSearch.results?.food?.compactFoodsToDomain() ?: emptyList()
                val totalPage = foodsSearch.totalResults.toIntOrNull() ?: dishes.size

                val prevKey = if (currentPage == DEFAULT_POSITION) null else currentPage - DEFAULT_PAGING_STEP
                val nextKey = if (countResults(currentPage, pageSize) <= totalPage) currentPage + DEFAULT_PAGING_STEP else null

                LoadResult.Page(
                    data = dishes,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            } catch (ex: Exception) {
                LoadResult.Error(ex)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, Dish>): Int? {
            return state.anchorPosition
        }
    }

    private fun countResults(currentPage: Int, pageSize: Int) =
        (currentPage + DEFAULT_PAGING_STEP) * pageSize

}
