package com.elta.android.data.features.diary.medicines.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinTypeDbEntity
import com.elta.android.data.features.diary.medicines.cache.conditions.InsulinMedicamentConditions
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinMedicamentDbEntity
import javax.inject.Inject

class DbInsulinMedicamentCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<InsulinMedicamentDbEntity>(factory) {

    override val classToken: Class<InsulinMedicamentDbEntity> = InsulinMedicamentDbEntity::class.java

    override fun getAll(condition: Condition): List<InsulinMedicamentDbEntity> {
        return when(condition) {
            is InsulinMedicamentConditions.ByInsulinType -> getMedicinesByInsulinType(condition.insulinType)
            else -> super.getAll(condition)
        }
    }

    private fun getMedicinesByInsulinType(type: InsulinTypeDbEntity): List<InsulinMedicamentDbEntity> {
        return box
            .query()
            .filter { it.insulinType == type }
            .build()
            .find()
    }
}
