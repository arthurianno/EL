package com.elta.android.presentation.features.calcutator.model // ktlint-disable filename

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.Serving

internal fun Dish.toUi(): DishUi =
    DishUi(
        id = id,
        localId = localId,
        name = name,
        type = type,
        brandName = brandName,
        isVerification = isVerification,
        servings = servings.map { it.toUi() },
        servingSelect = servingSelect.toUi(),
        servingAmount = servingAmount,
        breadUnits = breadUnits
    )

internal fun DishUi.toDomain(): Dish =
    Dish(
        id = id,
        localId = localId,
        name = name,
        type = type,
        brandName = brandName,
        servings = servings.map { it.toDomain() },
        servingSelect = servingSelect.toDomain(),
        servingAmount = servingAmount,
        isVerification = isVerification,
        breadUnits = breadUnits
    )

internal fun List<Dish>.toUi(): List<DishUi> =
    map { it.toUi() }

internal fun Serving.toUi(): ServingUi =
    ServingUi(
        id = id,
        servingDescription = servingDescription,
        measurementDescription = measurementDescription,
        numberOfUnits = numberOfUnits,
        calories = calories,
        proteins = proteins,
        fats = fats,
        carbs = carbs
    )

internal fun ServingUi.toDomain(): Serving =
    Serving(
        id = id,
        measurementDescription = measurementDescription,
        servingDescription = servingDescription,
        numberOfUnits = numberOfUnits,
        calories = calories,
        proteins = proteins,
        fats = fats,
        carbs = carbs
    )

internal fun List<DishUi>.toDomain(): List<Dish> =
    map { it.toDomain() }

internal fun servingUiEmpty() = Serving.empty().toUi()
