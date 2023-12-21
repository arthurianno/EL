package com.elta.android.presentation.features.calcutator.mappers // ktlint-disable filename

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.calculator.model.MetricServingLink
import com.elta.android.domain.features.calculator.model.Product
import com.elta.android.domain.features.calculator.model.Serving
import com.elta.android.presentation.features.calcutator.custom.model.ProductUiEntity
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.products.model.ServingUiEntity
import java.util.UUID

internal fun Dish.toUi(): DishUiEntity =
    DishUiEntity(
        id = id,
        localId = localId,
        name = name,
        type = type,
        brandName = brandName,
        isVerified = isVerified,
        servings = servings.map { it.toUi() },
        servingSelect = servingSelect.toUi(),
        servingAmount = servingAmount.toString(),
        servingCalories = selectServingCalories(),
        breadUnits = breadUnits?.toString()
    )

internal fun Product.toDish(): Dish {
    val serving = Serving(
        id = servingId,
        calories = calories?.toDouble(),
        proteins = protein?.toDouble(),
        fats = fat?.toDouble(),
        carbohydrate = carbohydrate?.toDouble(),
        metricServingLink = metricServingLink,
        numberOfUnits = metricServingAmount
    )
    return Dish(
        id = foodId,
        localId = UUID.randomUUID().toString(),
        name = foodName,
        type = DishType.Custom,
        brandName = "",
        servings = listOf(serving),
        servingSelect = serving,
        servingAmount = metricServingAmount,
        breadUnits = carbohydrate?.let { calculateBreadUnits(it.toDouble()) },
        isVerified = false
    )
}

internal fun DishUiEntity.toDomain(): Dish =
    Dish(
        id = id,
        localId = localId,
        name = name,
        type = type,
        brandName = brandName,
        servings = servings.map { it.toDomain() },
        servingSelect = servingSelect.toDomain(servingAmount.toDouble()),
        servingAmount = servingAmount.toDouble(),
        breadUnits = breadUnits?.toDoubleOrNull(),
        isVerified = isVerified
    )

internal fun List<Dish>.toUi(): List<DishUiEntity> =
    map { it.toUi() }

internal fun ProductUiEntity.toProduct(previousDish: DishUiEntity?): Product = Product(
    foodId = previousDish?.id ?: UUID.randomUUID().toString(),
    foodName = name.orEmpty(),
    servingId = previousDish?.servings?.firstOrNull()?.id ?: UUID.randomUUID().toString(),
    metricServingLink = metricServingLink ?: MetricServingLink(0, ""),
    metricServingAmount = numberOfUnits ?: METRIC_SERVING_AMOUNT_DEFAULT,
    carbohydrate = carbohydrate,
    fat = fat,
    calories = calories,
    protein = protein,
)

internal fun Product.toDishUi(): DishUiEntity = toDish().toUi()

private fun Serving.toUi(): ServingUiEntity =
    ServingUiEntity(
        id = id,
        idMetricServing = metricServingLink.id,
        nameMetricServing = metricServingLink.name,
        numberOfUnits = numberOfUnits.toString(),
        calories = calories?.format().orEmpty(),
        protein = proteins?.format().orEmpty(),
        fat = fats?.format().orEmpty(),
        carbohydrate = carbohydrate?.format()
    )

internal fun ServingUiEntity.toDomain(servingAmount: Double? = null): Serving =
    Serving(
        id = id,
        metricServingLink = MetricServingLink(idMetricServing, nameMetricServing),
        numberOfUnits = servingAmount ?: numberOfUnits.toDouble(),
        calories = calories.toDoubleOrNull() ?: ZERO_COUNT_DOUBLE,
        proteins = protein.toDoubleOrNull() ?: ZERO_COUNT_DOUBLE,
        fats = fat.toDoubleOrNull() ?: ZERO_COUNT_DOUBLE,
        carbohydrate = carbohydrate?.toDoubleOrNull() ?: ZERO_COUNT_DOUBLE
    )

internal fun List<DishUiEntity>.toDomain(): List<Dish> =
    map { it.toDomain() }

internal fun emptyServing() = Serving.empty().toUi()

internal fun ServingUiEntity.toNewAmount(amount: Double): ServingUiEntity {
    return copy(
        calories = calories.toCalculate(amount, numberOfUnits),
        protein = protein.toCalculate(amount, numberOfUnits),
        fat = fat.toCalculate(amount, numberOfUnits),
        carbohydrate = carbohydrate?.toCalculate(amount, numberOfUnits)
    )
}

const val METRIC_SERVING_AMOUNT_DEFAULT = 1.0
const val CARBOHYDRATE_DEFAULT = 1
