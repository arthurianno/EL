package com.elta.android.data.features.diary.insulin.cache.medicines

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.diary.insulin.cache.insulin.InsulinTypeDbEntity
import javax.inject.Inject

class DbMedicinesCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<MedicamentDbEntity>(factory) {

    override val classToken: Class<MedicamentDbEntity> = MedicamentDbEntity::class.java

    override fun getAll(condition: Condition): List<MedicamentDbEntity> {
        return when(condition) {
            is MedicinesConditions.ByInsulinType -> getMedicinesByInsulinType(condition.insulinType)
            else -> super.getAll(condition)
        }
    }

    private fun getMedicinesByInsulinType(type: InsulinTypeDbEntity): List<MedicamentDbEntity> {
        return box
            .query()
            .filter { it.insulinType == type }
            .build()
            .find()
    }
}
