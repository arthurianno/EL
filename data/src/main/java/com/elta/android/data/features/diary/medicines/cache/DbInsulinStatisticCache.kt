package com.elta.android.data.features.diary.medicines.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinStatisticDbEntity
import javax.inject.Inject

class DbInsulinStatisticCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<InsulinStatisticDbEntity>(factory) {

    override val classToken: Class<InsulinStatisticDbEntity> = InsulinStatisticDbEntity::class.java

    override fun getAll(condition: Condition): List<InsulinStatisticDbEntity> {
        val list = super.getAll(condition)
        return list.ifEmpty {
            listOf(InsulinStatisticDbEntity.empty())
        }
    }
}
