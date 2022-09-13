package com.elta.android.data.features.diary.insulin.cache

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class DrugCachedDto(
    @Id(assignable = true) var id: Long,
    val drug: String,
    val insulinType: String
)
