package com.elta.android.data.features.diary.medicines.cache.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class MedicamentDBEntity(
    @Id(assignable = true)
    var id: Long,
    val name: String,
    val other: Boolean,
    val deleted: Boolean,
    val touchedAt: Long,
    val lastUsed: Long?,
)
