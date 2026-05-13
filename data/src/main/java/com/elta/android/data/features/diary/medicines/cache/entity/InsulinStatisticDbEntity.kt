package com.elta.android.data.features.diary.medicines.cache.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class InsulinStatisticDbEntity(
    @Id(assignable = true) var id: Long = 0,
    val bolusInsulinTypes: List<String>,
    val basalInsulinTypes: List<String>,
    val countryCode: String? = null
) {
    companion object {
        fun empty(countryCode: String? = null) = InsulinStatisticDbEntity(
            id = 0,
            basalInsulinTypes = emptyList(),
            bolusInsulinTypes = emptyList(),
            countryCode = countryCode
        )
    }
}
