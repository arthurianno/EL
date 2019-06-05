package com.elta.android.data.features.devices.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class GlucometerInfoCachedDto(
    @Id(assignable = true) var id: Long,
    val secondaryId: String,
    val deviceDate: String? = null,
    val syncDate: String? = null,
    val temperature: Int? = null,
    val batteryLevel: Int? = null,
    val software: Double? = null,
    val hardware: Double? = null
)