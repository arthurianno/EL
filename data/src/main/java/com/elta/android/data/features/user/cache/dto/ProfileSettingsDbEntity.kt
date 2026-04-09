package com.elta.android.data.features.user.cache.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ProfileSettingsDbEntity(
    @Id(assignable = true) var id: Long,
    val isOnboarded: Boolean,
    val glucoseFormat: String,
    val countryCode: String? = null
)
