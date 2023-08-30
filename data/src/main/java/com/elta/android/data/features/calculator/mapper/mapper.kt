package com.elta.android.data.features.calculator.mapper // ktlint-disable filename

import com.elta.android.data.features.calculator.cache.model.DishDbEntity
import com.elta.android.data.features.calculator.model.FoodBrandResponse
import com.elta.android.data.features.calculator.model.FoodGenericResponse
import com.elta.android.data.features.calculator.model.FoodNetworkEntity
import com.elta.android.data.features.calculator.model.ProductResponse
import com.elta.android.data.features.calculator.model.ServingNetworkEntity
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.calculator.model.Serving
import java.util.UUID

internal fun FoodGenericResponse.Food.toDomain(): Dish {
    val dishType = foodType.getDishType()
    val servingSelect = servingsGeneric.servings.first().toDomain(dishType)
    return Dish(
        id = foodId,
        localId = getLocalId(),
        name = foodName,
        type = dishType,
        brandName = brandName.orEmpty(),
        servings = servingsGeneric.servings.toDomain(dishType),
        servingSelect = servingSelect,
        servingAmount = servingSelect.numberOfUnits,
        breadUnits = ZERO_DOUBLE
    )
}


internal fun FoodBrandResponse.Food.toDomain(): Dish {
    val dishType = foodType.getDishType()
    val servingSelect = servingsBrand.serving.toDomain(dishType)
    return Dish(
        id = foodId,
        localId = getLocalId(),
        name = foodName,
        type = DishType.Brand,
        brandName = brandName.orEmpty(),
        servings = listOf(servingsBrand.serving.toDomain(dishType)),
        servingSelect = servingSelect,
        servingAmount = servingSelect.numberOfUnits,
        breadUnits = ZERO_DOUBLE
    )
}


internal fun FoodNetworkEntity.toDomain(): Dish {
    val dishType = foodType.getDishType()
    return Dish(
        id = foodId,
        localId = "",
        name = foodName,
        type = dishType,
        brandName = brandName.orEmpty(),
        servings = servings.servings?.toDomain(dishType).orEmpty(),
        servingSelect = Serving.empty(),
        servingAmount = ONE_DOUBLE,
        breadUnits = ZERO_DOUBLE
    )
}

private fun List<ServingNetworkEntity>.toDomain(dishType: DishType): List<Serving> =
    map { serving ->
        serving.toDomain(dishType)
    }

private fun ServingNetworkEntity.toDomain(dishType: DishType): Serving {
    return Serving(
        id = servingId,
        calories = calories.toDoubleOrNull() ?: Double.NaN,
        proteins = protein.toDoubleOrNull() ?: Double.NaN,
        fats = fat.toDoubleOrNull() ?: Double.NaN,
        carbohydrate = carbohydrate.toDoubleOrNull() ?: Double.NaN,
        servingDescription = getServingDescription(dishType),
        numberOfUnits = getNumberOfUnits(dishType),
    )
}

private fun ServingNetworkEntity.getNumberOfUnits(dishType: DishType): Double {
    return when (dishType) {
        DishType.Generic -> numberOfUnits.toDoubleOrNull() ?: ONE_DOUBLE
        DishType.Brand -> metricServingAmount?.toDoubleOrNull() ?: ONE_DOUBLE
    }
}

private fun ServingNetworkEntity.getServingDescription(dishType: DishType): String {
    return when (dishType) {
        DishType.Generic -> measurementDescription
        DishType.Brand -> metricServingUnit.orEmpty()
    }
}


internal fun List<FoodNetworkEntity>.compactFoodsToDomain(): List<Dish> =
    map { it.toDomain() }

fun ProductResponse.toDomain(): Dish =
    Dish(
        id = id,
        localId = getLocalId(),
        name = name,
        type = type.getDishType(),
        brandName = "",
        servings = emptyList(),
        servingAmount = servingAmount,
        servingSelect = getServing(servingId, servingName),
        breadUnits = breadUnits
    )

@JvmName("toDomainProductResponse")
fun List<ProductResponse>.toDomain(): List<Dish> =
    map { it.toDomain() }

fun Dish.toNetwork(): ProductResponse =
    ProductResponse(
        id = id,
        name = name,
        type = type.name,
        servingId = servingSelect.id,
        servingName = servingSelect.servingDescription,
        servingAmount = servingAmount,
        breadUnits = breadUnits
    )

fun List<Dish>.toNetwork(): List<ProductResponse> =
    map { it.toNetwork() }

internal fun DishDbEntity.toDomain(): Dish =
    Dish(
        id = dishId,
        localId = getLocalId(),
        name = name,
        type = type.getDishType(),
        brandName = "",
        servings = emptyList(),
        servingSelect = getServing(servingId, servingName),
        servingAmount = servingAmount,
        breadUnits = breadUnits
    )

internal fun Dish.toDb(): DishDbEntity =
    DishDbEntity(
        id = localId.hashCode().toLong(),
        dishId = id,
        name = name,
        type = type.name,
        servingId = servingSelect.id,
        servingName = servingSelect.servingDescription,
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
        numberOfUnits = ONE_DOUBLE,
        calories = ZERO_DOUBLE,
        proteins = ZERO_DOUBLE,
        fats = ZERO_DOUBLE,
        carbohydrate = ZERO_DOUBLE
    )

private fun getLocalId(): String = UUID.randomUUID().toString()

private fun String.getDishType(): DishType =
    runCatching { DishType.valueOf(this) }.getOrNull() ?: DishType.Brand

private const val ZERO_DOUBLE = 0.0
private const val ONE_DOUBLE = 1.0