package com.elta.android.data.features.diary.medicines.cache.conditions

import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.diary.medicines.cache.entity.InsulinTypeDbEntity

sealed class InsulinMedicamentConditions : Condition {
    data class ByInsulinType(val insulinType: InsulinTypeDbEntity) : InsulinMedicamentConditions()

}
