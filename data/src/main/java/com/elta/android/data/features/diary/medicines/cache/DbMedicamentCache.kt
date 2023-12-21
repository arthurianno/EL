package com.elta.android.data.features.diary.medicines.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.diary.medicines.cache.conditions.MedicamentConditions
import com.elta.android.data.features.diary.medicines.cache.entity.MedicamentDBEntity
import com.elta.android.data.features.diary.medicines.cache.entity.MedicamentDBEntity_
import io.objectbox.kotlin.and
import io.objectbox.kotlin.query
import io.objectbox.query.QueryBuilder
import javax.inject.Inject

class DbMedicamentCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<MedicamentDBEntity>(factory) {

    override val classToken: Class<MedicamentDBEntity> =
        MedicamentDBEntity::class.java

    override fun getAll(condition: Condition): List<MedicamentDBEntity> {
        return when (condition) {
            MedicamentConditions.LastUsed -> getLastUsed()
            else -> super.getAll(condition)
        }
    }

    private fun getLastUsed(): List<MedicamentDBEntity> {
        return box
            .query(MedicamentDBEntity_.lastUsed.notNull())
            .order(MedicamentDBEntity_.lastUsed, QueryBuilder.DESCENDING)
            .build()
            .find(LAST_USED_OFFSET, LAST_USED_LIMIT)
    }
}

private const val LAST_USED_OFFSET = 0L
private const val LAST_USED_LIMIT = 5L
