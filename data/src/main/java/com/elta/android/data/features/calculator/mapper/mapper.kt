package com.elta.android.data.features.calculator.mapper // ktlint-disable filename

import com.elta.android.data.features.calculator.cache.model.DishDbEntity
import com.elta.android.data.features.calculator.cache.model.ServingDbEntity
import com.elta.android.data.features.calculator.cache.model.VerifiedProductDbEntity
import com.elta.android.data.features.calculator.model.FoodBrandResponse
import com.elta.android.data.features.calculator.model.FoodGenericResponse
import com.elta.android.data.features.calculator.model.FoodNetworkEntity
import com.elta.android.data.features.calculator.model.ProductResponse
import com.elta.android.data.features.calculator.model.ServingNetworkEntity
import com.elta.android.data.features.calculator.model.VerifiedProductResponse
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
        breadUnits = ZERO_DOUBLE,
        isVerified = false
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
        breadUnits = ZERO_DOUBLE,
        isVerified = false
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
        breadUnits = ZERO_DOUBLE,
        isVerified = false
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
        DishType.Verified -> ONE_DOUBLE
    }
}

private fun ServingNetworkEntity.getServingDescription(dishType: DishType): String {
    return when (dishType) {
        DishType.Generic -> measurementDescription
        DishType.Brand -> metricServingUnit.orEmpty()
        DishType.Verified -> ""
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
        brandName = brandName,
        servings = listOf(getServings()),
        servingAmount = servingAmount,
        servingSelect = getServings(),
        breadUnits = breadUnits,
        isVerified = isVerified ?: false
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
        breadUnits = breadUnits,
        brandName = brandName,
        calories = servingSelect.calories ?: ZERO_DOUBLE,
        proteins = servingSelect.proteins ?: ZERO_DOUBLE,
        fats = servingSelect.fats ?: ZERO_DOUBLE,
        carbohydrates = servingSelect.carbohydrate,
        isVerified = isVerified
    )

fun List<VerifiedProductResponse>.verifiedProductToDish(): List<Dish> =
    map { it.toDomain() }

private fun VerifiedProductResponse.toDomain(): Dish {
    val dishServings = servings.map { it.toDomain() }
    return Dish(
        id = foodId,
        localId = getLocalId(),
        name = foodName,
        type = DishType.Verified,
        brandName = "",
        servings = dishServings,
        servingSelect = Serving.empty(),
        servingAmount = ONE_DOUBLE,
        breadUnits = ZERO_DOUBLE,
        isVerified = isVerified
    )
}


private fun VerifiedProductResponse.Serving.toDomain(): Serving = Serving(
    id = servingId,
    calories = calories,
    proteins = protein,
    fats = fat,
    carbohydrate = carbohydrate,
    servingDescription = metricServingUnit,
    numberOfUnits = metricServingAmount
)

fun List<Dish>.toVerifiedDB(): List<VerifiedProductDbEntity> =
    map { it.toVerifiedDb(it.localId.hashCode().toLong()) }

private fun Dish.toVerifiedDb(dbId: Long): VerifiedProductDbEntity =
    VerifiedProductDbEntity(
        id = dbId,
        dishId = id,
        foodName = name,
        type = type.name,
        brandName = brandName,
        servings = servings.map { it.toDb(dbId) },
        servingSelect = servingSelect.toDb(dbId),
        servingAmount = servingAmount,
        breadUnits = breadUnits
    )

fun VerifiedProductDbEntity.toDomain(): Dish = Dish(
        id = dishId,
        localId = getLocalId(),
        name = foodName,
        type = type.getDishType(),
        brandName = brandName,
        servings = servings.map { it.getServings() },
        servingSelect = servingSelect.getServings(),
        servingAmount = servingAmount,
        breadUnits = breadUnits,
        isVerified = true
    )

fun List<Dish>.toNetwork(): List<ProductResponse> =
    map { it.toNetwork() }

internal fun DishDbEntity.toDomain(): Dish =
    Dish(
        id = dishId,
        localId = getLocalId(),
        name = name,
        type = type.getDishType(),
        brandName = brandName,
        servings = listOf(servingSelect.getServings()),
        servingSelect = servingSelect.getServings(),
        servingAmount = servingAmount,
        breadUnits = breadUnits,
        isVerified = isVerified
    )

private fun Serving.toDb(dbId: Long) = ServingDbEntity(
    id = dbId,
    servingId = id,
    calories = calories,
    proteins = proteins,
    carbohydrate = carbohydrate,
    fats = fats,
    servingDescription = servingDescription,
    numberOfUnits = numberOfUnits
)

internal fun Dish.toDb(): DishDbEntity {
    val dbId = localId.hashCode().toLong()
    return DishDbEntity(
        id = dbId,
        dishId = id,
        name = name,
        type = type.name,
        brandName = brandName,
        servingSelect = servingSelect.toDb(dbId),
        servingAmount = servingAmount,
        breadUnits = breadUnits,
        isVerified = isVerified
    )
}


internal fun List<Dish>.toDb(): List<DishDbEntity> =
    map { it.toDb() }

@JvmName("toDomainDishDbEntity")
internal fun List<DishDbEntity>.toDomain(): List<Dish> =
    map { it.toDomain() }

private fun ServingDbEntity.getServings() = Serving(
    id = servingId,
    calories = calories,
    proteins = proteins,
    fats = fats,
    carbohydrate = carbohydrate,
    servingDescription = servingDescription,
    numberOfUnits = numberOfUnits
)

private fun ProductResponse.getServings() = Serving(
    id = id,
    calories = calories,
    proteins = proteins,
    fats = fats,
    carbohydrate = carbohydrates,
    servingDescription = servingName,
    numberOfUnits = servingAmount
)

private fun getLocalId(): String = UUID.randomUUID().toString()

private fun String.getDishType(): DishType =
    runCatching { DishType.valueOf(this) }.getOrNull() ?: DishType.Brand

private const val ZERO_DOUBLE = 0.0
private const val ONE_DOUBLE = 1.0