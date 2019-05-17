package com.elta.android.data.features.sync.cache

import com.elta.android.data.features.common.cache.Condition

sealed class LocalSyncChangesConditions : Condition {
    data class ByClassName(val className: String) : LocalSyncChangesConditions()
    data class ByClassNameAndId(val id: String, val className: String) : LocalSyncChangesConditions()
}