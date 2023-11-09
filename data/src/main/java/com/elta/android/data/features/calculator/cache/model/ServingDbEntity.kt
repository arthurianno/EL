package com.elta.android.data.features.calculator.cache.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ServingDbEntity(
    @Id(assignable = false) var id: Long,
    val servingId: String,
    val calories: Double?,
    val proteins: Double?,
    val fats: Double?,
    val carbohydrate: Double,
    val idServingMetrics: Int,
    val nameServingMetrics: String,
    val numberOfUnits: Double,
)
