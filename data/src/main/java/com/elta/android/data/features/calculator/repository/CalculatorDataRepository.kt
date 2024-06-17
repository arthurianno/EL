package com.elta.android.data.features.calculator.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.elta.android.common.di.qualifires.Paging
import com.elta.android.common.di.qualifires.PagingType
import com.elta.android.common.utils.takeFirst
import com.elta.android.data.core.paging.BasePagingSource
import com.elta.android.data.core.paging.QueryPaging
import com.elta.android.data.features.calculator.datasource.calculator.CalculatorCacheDataSource
import com.elta.android.data.features.calculator.datasource.fatsecret.FatSecretDataSource
import com.elta.android.data.features.calculator.datasource.verified.ProductsDataSource
import com.elta.android.domain.common.ReturnDataHandler
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.calculator.model.MetricServingLink
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import com.elta.android.domain.features.user.model.Diabetes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val HISTORY_LIST_LENGTH = 5

class CalculatorDataRepository @Inject constructor(
    private val fatSecretDataSource: FatSecretDataSource,
    private val cache: CalculatorCacheDataSource,
    override val dispatcher: CoroutineDispatcher,
    private val productsDataSource: ProductsDataSource,
    @Paging(PagingType.FatSecret) private val dishesPagingSource: BasePagingSource,
    @Paging(PagingType.Products) private val productsPagingSource: BasePagingSource,
) : CalculatorRepository {

    override val addDishFragmentResult = ReturnDataHandler.resultObject<Dish>()
    override val calculatorFragmentResult = ReturnDataHandler.resultObject<List<Dish>>()

    override fun getDish(id: String, type: DishType): Flow<Dish> {
        return when (type) {
            DishType.Verified, DishType.Custom -> {
                productsDataSource.getProduct(id)
                    .flowOn(dispatcher)
            }

            DishType.Generic, DishType.Brand -> {
                fatSecretDataSource.getFood(id, type)
                    .flowOn(dispatcher)
            }
        }
    }


    override fun searchDishes(name: String): Flow<PagingData<Dish>> {
        dishesPagingSource.setQuery(QueryPaging.Dishes(name))
        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                prefetchDistance = DEFAULT_PREFETCH_DISTANCE,
                initialLoadSize = DEFAULT_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { dishesPagingSource.pagingSource }
        )
            .flow
            .flowOn(dispatcher)
    }


    override fun getHistoryList(): Flow<List<String>> =
        cache.getHistoryWords()
            .map { it.takeFirst(HISTORY_LIST_LENGTH) }
            .flowOn(dispatcher)

    override suspend fun saveWordToHistory(word: String) {
        cache.saveWordToHistory(word)
    }

    override suspend fun getProducts(
        name: String,
        onlyCustom: Boolean,
        diabetes: Diabetes?
    ): Flow<PagingData<Dish>> {
        productsPagingSource.setQuery(QueryPaging.Product(name, onlyCustom, diabetes))
        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                prefetchDistance = DEFAULT_PREFETCH_DISTANCE,
                initialLoadSize = DEFAULT_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { productsPagingSource.pagingSource }
        )
            .flow
            .flowOn(dispatcher)

    }

    override fun getServingsProduct(): Flow<List<MetricServingLink>> {
        return productsDataSource.getServingsProduct()
            .flowOn(dispatcher)
    }

    override fun getLocalDishes(): Flow<List<Dish>> =
        cache.getDishesFromCache()
            .flowOn(dispatcher)

    override suspend fun saveLocalDishes(dishes: List<Dish>) {
        cache.cachedDishes(dishes)
    }

    override suspend fun clearLocalDishes() {
        cache.clearDishesCache()
    }
}

const val DEFAULT_PAGE_SIZE = 50
const val DEFAULT_PREFETCH_DISTANCE = 2
