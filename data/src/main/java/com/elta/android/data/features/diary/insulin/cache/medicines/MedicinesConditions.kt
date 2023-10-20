package com.elta.android.data.features.diary.insulin.cache.medicines

import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.diary.insulin.cache.insulin.InsulinTypeDbEntity

sealed class MedicinesConditions : Condition {
    data class ByInsulinType(val insulinType: InsulinTypeDbEntity) : MedicinesConditions()

}
