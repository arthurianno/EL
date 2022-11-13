package com.elta.android.data.features.calculator.mapper // ktlint-disable filename

import com.elta.android.data.features.calculator.dto.CompactFoodDto
import com.elta.android.data.features.calculator.dto.FoodBrandDto
import com.elta.android.data.features.calculator.dto.FoodGenericDto
import com.elta.android.data.features.calculator.dto.ServingDto
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.calculator.model.Portion

internal fun FoodGenericDto.Food.toDomain(): Dish =
    Dish(
        id = foodId,
        name = foodName,
        type = DishType.Generic,
        portions = servingsGeneric.servings.foodsToDomain(),
        isVerification = false,
        breadUnits = 0.0
    )

internal fun FoodBrandDto.Food.toDomain(): Dish =
    Dish(
        id = foodId,
        name = foodName,
        type = DishType.Brand,
        portions = listOf(servingsBrand.serving.toDomain()),
        isVerification = false,
        breadUnits = 0.0
    )

internal fun ServingDto.toDomain(): Portion =
    Portion(
        id = servingId,
        description = servingDescription,
        metricUnit = metricServingUnit,
        metricAmount = metricServingAmount.toDouble(),
        calories = calories.toDouble(),
        proteins = protein.toDouble(),
        fats = fat.toDouble(),
        carbs = carbohydrate.toDouble()
    )

internal fun List<ServingDto>.foodsToDomain(): List<Portion> =
    map { it.toDomain() }

internal fun CompactFoodDto.toDomain(): Dish =
    Dish(
        id = foodId,
        name = foodName,
        type = DishType.valueOf(foodType),
        portions = emptyList(),
        isVerification = false,
        breadUnits = 0.0
    )

internal fun List<CompactFoodDto>.compactFoodsToDomain(): List<Dish> =
    map { it.toDomain() }
