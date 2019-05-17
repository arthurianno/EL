package com.elta.android.data.features.sync.cache.dto

import com.elta.android.data.features.common.dto.DataWithStateDto
import com.elta.android.data.features.common.dto.StateDto
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class LocalSyncCachedDto(
    @Id(assignable = true) var _id: Long = 0,
    override val id: String,
    @Convert(converter = StateDtoConverter::class, dbType = String::class) override val state: StateDto,
    val className: String,
    var meta: String? = null
) : DataWithStateDto