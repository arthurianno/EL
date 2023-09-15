package com.elta.android.data.features.calculator.cache

import com.elta.android.data.features.calculator.cache.model.VerifiedProductDbEntity
import com.elta.android.data.features.calculator.cache.model.VerifiedProductDbEntity_
import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.Condition
import io.objectbox.query.QueryBuilder
import javax.inject.Inject

class VerifiedProductCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<VerifiedProductDbEntity>(factory) {

    override val classToken: Class<VerifiedProductDbEntity> = VerifiedProductDbEntity::class.java

    override fun get(condition: Condition): VerifiedProductDbEntity? {
        return when(condition) {
            is VerifiedProductConditions.ById -> getVerifiedProduct(condition.id)
            else -> super.get(condition)
        }
    }

    private fun getVerifiedProduct(id: String): VerifiedProductDbEntity? {
        return box.query()
            .equal(VerifiedProductDbEntity_.dishId, id, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build()
            .findFirst()
    }
}
