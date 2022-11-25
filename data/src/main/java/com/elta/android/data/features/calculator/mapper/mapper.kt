package com.elta.android.data.features.calculator.mapper // ktlint-disable filename

import com.elta.android.data.features.calculator.cache.model.DishDbEntity
import com.elta.android.data.features.calculator.model.CompactFoodNetworkEntity
import com.elta.android.data.features.calculator.model.FoodBrandResponse
import com.elta.android.data.features.calculator.model.FoodGenericResponse
import com.elta.android.data.features.calculator.model.ProductResponse
import com.elta.android.data.features.calculator.model.ServingNetworkEntity
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.calculator.model.Serving
import java.util.UUID

internal fun FoodGenericResponse.Food.toDomain(): Dish =
    Dish(
        id = foodId,
        localId = getLocalId(),
        name = foodName,
        type = DishType.Generic,
        brandName = brandName.orEmpty(),
        servings = servingsGeneric.servings.servingToDomain(),
        servingSelect = servingsGeneric.servings.first().toDomain(),
        servingAmount = 1.0,
        isVerification = false,
        breadUnits = 0.0
    )

internal fun FoodBrandResponse.Food.toDomain(): Dish =
    Dish(
        id = foodId,
        localId = getLocalId(),
        name = foodName,
        type = DishType.Brand,
        brandName = brandName.orEmpty(),
        servings = listOf(servingsBrand.serving.toDomain()),
        servingSelect = servingsBrand.serving.toDomain(),
        servingAmount = 1.0,
        isVerification = false,
        breadUnits = 0.0
    )

internal fun ServingNetworkEntity.toDomain(): Serving =
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

internal fun List<ServingNetworkEntity>.servingToDomain(): List<Serving> =
    map { it.toDomain() }

internal fun CompactFoodNetworkEntity.toDomain(): Dish =
    Dish(
        id = foodId,
        localId = "",
        name = foodName,
        type = DishType.valueOf(foodType),
        brandName = brandName.orEmpty(),
        servings = emptyList(),
        servingSelect = Serving.empty(),
        servingAmount = 1.0,
        isVerification = false,
        breadUnits = 0.0
    )

internal fun List<CompactFoodNetworkEntity>.compactFoodsToDomain(): List<Dish> =
    map { it.toDomain() }

internal fun ProductResponse.toDomain(): Dish =
    Dish(
        id = id,
        localId = getLocalId(),
        name = name,
        type = DishType.valueOf(type),
        brandName = "",
        servings = emptyList(),
        servingAmount = servingAmount,
        servingSelect = getServing(servingId, servingName),
        isVerification = false,
        breadUnits = breadUnits
    )

@JvmName("toDomainProductResponse")
internal fun List<ProductResponse>.toDomain(): List<Dish> =
    map { it.toDomain() }

internal fun DishDbEntity.toDomain(): Dish =
    Dish(
        id = dishId,
        localId = getLocalId(),
        name = name,
        type = DishType.valueOf(type),
        brandName = "",
        servings = emptyList(),
        servingSelect = getServing(servingId, servingName),
        servingAmount = servingAmount,
        isVerification = false,
        breadUnits = breadUnits
    )

internal fun Dish.toDb(): DishDbEntity =
    DishDbEntity(
        id = localId.hashCode().toLong(),
        dishId = id,
        name = name,
        type = type.name,
        servingId = servingSelect.id,
        servingName = servingSelect.measurementDescription,
        servingAmount = servingAmount,
        breadUnits = breadUnits
    )

internal fun List<Dish>.toDb(): List<DishDbEntity> =
    map { it.toDb() }

@JvmName("toDomainDishDbEntity")
internal fun List<DishDbEntity>.toDomain(): List<Dish> =
    map { it.toDomain() }

private fun getServing(id: String, name: String) =
    Serving(
        id = id,
        servingDescription = name,
        measurementDescription = name,
        numberOfUnits = 1.0,
        calories = 0.0,
        proteins = 0.0,
        fats = 0.0,
        carbs = 0.0
    )

private fun getLocalId(): String = UUID.randomUUID().toString()
