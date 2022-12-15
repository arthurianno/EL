package com.elta.android.presentation.features.calcutator.model // ktlint-disable filename

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.Serving

internal fun Dish.toUi(): DishUiEntity =
    DishUiEntity(
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

internal fun DishUiEntity.toDomain(): Dish =
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

internal fun List<Dish>.toUi(): List<DishUiEntity> =
    map { it.toUi() }

internal fun Serving.toUi(): ServingUiEntity =
    ServingUiEntity(
        id = id,
        servingDescription = servingDescription,
        numberOfUnits = numberOfUnits,
        calories = calories,
        proteins = proteins,
        fats = fats,
        carbs = carbs
    )

internal fun ServingUiEntity.toDomain(): Serving =
    Serving(
        id = id,
        servingDescription = servingDescription,
        numberOfUnits = numberOfUnits,
        calories = calories,
        proteins = proteins,
        fats = fats,
        carbs = carbs
    )

internal fun List<DishUiEntity>.toDomain(): List<Dish> =
    map { it.toDomain() }

internal fun emptyServing() = Serving.empty().toUi()
