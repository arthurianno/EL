package com.elta.android.data.features.diary.insulin.cache.insulin

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class InsulinTypeDbEntity(
    @Id(assignable = true) var id: Long,
    val code: String,
    val name: String
)