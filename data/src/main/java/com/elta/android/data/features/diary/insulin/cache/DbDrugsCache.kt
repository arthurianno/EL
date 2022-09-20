package com.elta.android.data.features.diary.insulin.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.Condition
import io.objectbox.kotlin.query
import io.objectbox.query.QueryBuilder.StringOrder.CASE_SENSITIVE
import javax.inject.Inject

class DbDrugsCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<DrugCachedDto>(factory) {

    override val classToken: Class<DrugCachedDto> = DrugCachedDto::class.java

    override fun getAll(condition: Condition): List<DrugCachedDto> =
        when (condition) {
            is DrugConditions.ByInsulinType -> getAllByInsulinType(condition.insulinType)
            else -> super.getAll(condition)
        }

    private fun getAllByInsulinType(insulinType: String): List<DrugCachedDto> =
        box.query {
            equal(DrugCachedDto_.insulinType, insulinType, CASE_SENSITIVE)
        }.find()
}
