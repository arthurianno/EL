package com.elta.android.presentation.features.calcutator.model // ktlint-disable filename

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.Portion

internal fun Dish.toUi(): DishUi =
    DishUi(
        id = id,
        name = name,
        type = type,
        isVerification = isVerification,
        portions = portions.map { it.toUi() },
        breadUnits = breadUnits
    )

internal fun DishUi.toDomain(): Dish =
    Dish(
        id = id,
        name = name,
        type = type,
        portions = portions.map { it.toDomain() },
        isVerification = isVerification,
        breadUnits = breadUnits
    )

internal fun List<Dish>.toUi(): List<DishUi> =
    map { it.toUi() }

internal fun Portion.toUi(): PortionUi =
    PortionUi(
        id = id,
        description = description,
        metricUnit = metricUnit,
        metricAmount = metricAmount,
        calories = calories,
        proteins = proteins,
        fats = fats,
        carbs = carbs
    )

internal fun PortionUi.toDomain(): Portion =
    Portion(
        id = id,
        description = description,
        metricUnit = metricUnit,
        metricAmount = metricAmount,
        calories = calories,
        proteins = proteins,
        fats = fats,
        carbs = carbs
    )
