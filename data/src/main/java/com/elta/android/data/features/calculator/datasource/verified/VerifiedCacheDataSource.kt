package com.elta.android.data.features.calculator.datasource.verified

import com.elta.android.common.errors.DishError
import com.elta.android.data.features.calculator.cache.VerifiedProductConditions
import com.elta.android.data.features.calculator.cache.model.VerifiedProductDbEntity
import com.elta.android.data.features.calculator.mapper.toDomain
import com.elta.android.data.features.calculator.mapper.toVerifiedDB
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.domain.features.calculator.model.Dish
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class VerifiedCacheDataSource @Inject constructor(
    private val dishCache: Cache<VerifiedProductDbEntity>
) {
    fun saveProducts(dishes: List<Dish>) {
        with(dishCache) {
            val verifiedProductDbEntity = dishes.toVerifiedDB()
            delete(CommonConditions.All)
            add(verifiedProductDbEntity)
        }
    }

    fun getProducts(name: String): Flow<List<Dish>> =
        flowOf(
            dishCache.getAll(CommonConditions.All)
                .filter { verifiedProduct -> verifiedProduct.foodName.contains(name, true) }
                .map { it.toDomain() }
        )

    fun getProduct(id: String): Flow<Dish> = flowOf(
        dishCache.get(VerifiedProductConditions.ById(id))
            ?.toDomain() ?: throw DishError.NotFound
    )

}