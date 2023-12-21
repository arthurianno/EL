package com.elta.android.data.features.diary.medicines.cache.conditions

import com.elta.android.data.features.common.cache.Condition

sealed class MedicamentConditions : Condition {
    object LastUsed : MedicamentConditions()
}