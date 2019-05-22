package com.elta.android.data.features.sync.cache

import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.common.dto.StateDto

sealed class LocalSyncChangesConditions : Condition {
    data class ByClassName(val className: String) : LocalSyncChangesConditions()
    data class ByClassNameAndId(val id: String, val className: String) : LocalSyncChangesConditions()
    data class ByClassAndState(val className: String, val state: StateDto) : LocalSyncChangesConditions()
}