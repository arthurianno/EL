package com.elta.android.data.features.calculator.mapper // ktlint-disable filename

import com.elta.android.data.features.calculator.dto.CompactFoodDto
import com.elta.android.data.features.calculator.dto.FoodBrandDto
import com.elta.android.data.features.calculator.dto.FoodGenericDto
import com.elta.android.data.features.calculator.dto.ServingDto
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.calculator.model.Serving
import java.util.UUID

internal fun FoodGenericDto.Food.toDomain(): Dish =
    Dish(
        id = UUID.randomUUID().toString(),
        dishId = foodId,
        name = foodName,
        type = DishType.Generic,
        servings = servingsGeneric.servings.foodsToDomain(),
        servingSelect = servingsGeneric.servings.first().toDomain(),
        servingAmount = 1.0,
        isVerification = false,
        breadUnits = 0.0
    )

internal fun FoodBrandDto.Food.toDomain(): Dish =
    Dish(
        id = UUID.randomUUID().toString(),
        dishId = foodId,
        name = foodName,
        type = DishType.Brand,
        servings = listOf(servingsBrand.serving.toDomain()),
        servingSelect = servingsBrand.serving.toDomain(),
        servingAmount = 1.0,
        isVerification = false,
        breadUnits = 0.0
    )

internal fun ServingDto.toDomain(): Serving =
    Serving(
        id = servingId,
        servingDescription = servingDescription,
        measurementDescription = measurementDescription,
        numberOfUnits = numberOfUnits.toDouble(),
        calories = calories.toDouble(),
        proteins = protein.toDouble(),
        fats = fat.toDouble(),
        carbs = carbohydrate.toDouble()
    )

internal fun List<ServingDto>.foodsToDomain(): List<Serving> =
    map { it.toDomain() }

internal fun CompactFoodDto.toDomain(): Dish =
    Dish(
        id = "",
        dishId = foodId,
        name = foodName,
        type = DishType.valueOf(foodType),
        servings = emptyList(),
        servingSelect = Serving.empty(),
        servingAmount = 1.0,
        isVerification = false,
        breadUnits = 0.0
    )

internal fun List<CompactFoodDto>.compactFoodsToDomain(): List<Dish> =
    map { it.toDomain() }
