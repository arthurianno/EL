package com.elta.android.data.features.calculator.cache

import com.elta.android.data.features.common.cache.Condition

sealed class VerifiedProductConditions : Condition {

    data class ById(val id: String) : VerifiedProductConditions()
}
