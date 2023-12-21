package com.elta.android.data.features.calculator.cache.model

import com.elta.android.data.features.calculator.cache.converter.ServingDbEntityConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class DishDbEntity(
    @Id(assignable = true) var id: Long,
    val dishId: String,
    val name: String,
    val type: String,
    val brandName: String,

    @Convert(converter = ServingDbEntityConverter::class, dbType = String::class)
    val servingSelect: ServingDbEntity,

    val servingAmount: Double,
    val breadUnits: Double?,

    val isVerified: Boolean,
)
