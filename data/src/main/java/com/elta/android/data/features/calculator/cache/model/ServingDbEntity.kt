package com.elta.android.data.features.calculator.cache.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ServingDbEntity(
    @Id(assignable = true) var id: Long,
    val servingId: String,
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbohydrate: Double,
    val servingDescription: String,
    val numberOfUnits: Double,
)
