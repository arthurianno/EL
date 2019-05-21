package com.elta.android.data.features.devices.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class GlucometerCachedDto(
    @Id(assignable = true) var id: Long,
    val secondaryId: String,
    val address: String,
    val name: String?,
    var isPrimary: Boolean
)