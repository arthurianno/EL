package com.elta.android.data.features.common.cache

sealed class CommonConditions : Condition {

    data class ByIds(val ids: List<Long>) : CommonConditions()
    data class ById(val id: Long) : CommonConditions()
    object All : CommonConditions()
}