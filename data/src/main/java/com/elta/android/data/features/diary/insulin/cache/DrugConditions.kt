package com.elta.android.data.features.diary.insulin.cache

import com.elta.android.data.features.common.cache.Condition

sealed class DrugConditions : Condition {

    data class ByInsulinType(val insulinType: String) : DrugConditions()
}
