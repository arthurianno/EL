package com.elta.android.data.features.user.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ProfileCacheDto(
    @Id(assignable = true) var id: Long,
    val diabetes: String?,
    val weight: Double?,
    val gender: String?,
    val email: String?,
    val timeStamp: Long,

    // represents PersonDto
    val firstName: String?,
    val lastName: String?,

    // represents GlucoseLevelDto
    val minValue: Double?,
    val maxValue: Double?
)
