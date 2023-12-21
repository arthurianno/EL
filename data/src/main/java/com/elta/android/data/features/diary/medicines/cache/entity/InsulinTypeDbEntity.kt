package com.elta.android.data.features.diary.medicines.cache.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class InsulinTypeDbEntity(
    @Id(assignable = true) var id: Long,
    val code: String,
    val name: String
)