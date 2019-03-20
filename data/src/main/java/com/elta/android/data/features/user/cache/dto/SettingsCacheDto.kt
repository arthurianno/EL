package com.elta.android.data.features.user.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class SettingsCacheDto(
    @Id(assignable = true) var id: Long,
    val diabetType: String,
    val weight: Double,
    val gender: String,
    val email: String?,
    val timeStamp: Int,

    // represents PersonDto
    val firstName: String?,
    val lastName: String?,

    // represents GlucoseLevelDto
    val minValue: Double?,
    val maxValue: Double?
)
