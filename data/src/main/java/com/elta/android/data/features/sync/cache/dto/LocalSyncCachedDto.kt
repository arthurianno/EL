package com.elta.android.data.features.sync.cache.dto

import com.elta.android.data.features.common.dto.StateDto
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class LocalSyncCachedDto(
    @Id(assignable = true) var id: Long = 0,
    val secondaryId: String,
    @Convert(converter = StateDtoConverter::class, dbType = String::class) val state: StateDto,
    val className: String,
    var meta: String? = null
)