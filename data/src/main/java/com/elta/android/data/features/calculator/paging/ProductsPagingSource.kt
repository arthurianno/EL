package com.elta.android.data.features.calculator.paging

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.elta.android.data.core.paging.BasePagingSource
import com.elta.android.data.core.paging.QueryPaging
import com.elta.android.data.features.calculator.datasource.verified.ProductsDataSource
import com.elta.android.data.features.calculator.mapper.toDish
import com.elta.android.domain.features.calculator.model.Dish
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class ProductsPagingSource @Inject constructor(
    private val productsDataSource: ProductsDataSource,
    private val schedulersFacade: SchedulersFacade
) : BasePagingSource() {

    override val defaultPosition: Int = DEFAULT_POSITION

    private var query: QueryPaging.Product = QueryPaging.Product()

    override fun setQuery(queryPaging: QueryPaging) {
        this.query = (queryPaging as? QueryPaging.Product) ?: QueryPaging.Product()
    }

    override val pagingSource: RxPagingSource<Int, Dish> = object : RxPagingSource<Int, Dish>() {

        override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, Dish>> {
            val currentPage = params.key ?: DEFAULT_POSITION
            val pageSize = params.loadSize

            return productsDataSource.getProducts(
                customOnly = query.onlyCustom,
                foodName = query.name,
                pageIndex = currentPage,
                pageSize = pageSize,
                diabetesType = query.diabetesType,
            )
                .map { productsResponse ->
                    val dishes = productsResponse.toDish()
                    val totalPage = productsResponse.meta.totalItems
                    returnResult(
                        dishes,
                        currentPage,
                        totalPage,
                        pageSize
                    )
                }
                .onErrorReturn { ex -> LoadResult.Error(ex) }
                .subscribeOn(schedulersFacade.subscribeOn)
        }

        override fun getRefreshKey(state: PagingState<Int, Dish>): Int? {
            return state.anchorPosition
        }
    }

}

private const val DEFAULT_POSITION = 1
