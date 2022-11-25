package com.elta.android.data.features.calculator.cache.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class DishDbEntity(
    @Id(assignable = true) var id: Long,
    val dishId: String,
    val name: String,
    val type: String,
    val servingAmount: Double,
    val servingId: String,
    val servingName: String,
    val breadUnits: Double
)
