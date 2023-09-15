package com.elta.android.data.features.calculator.cache.model

import com.elta.android.data.features.calculator.cache.converter.ServingDbEntityConverter
import com.elta.android.data.features.calculator.cache.converter.ServingsDbEntityConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class VerifiedProductDbEntity(
    @Id(assignable = true) var id: Long,
    val dishId: String,
    val foodName: String,
    val type: String,
    val brandName: String,

    @Convert(converter = ServingsDbEntityConverter::class, dbType = String::class)
    val servings: List<ServingDbEntity>,

    @Convert(converter = ServingDbEntityConverter::class, dbType = String::class)
    val servingSelect: ServingDbEntity,

    val servingAmount: Double,
    val breadUnits: Double,
)
