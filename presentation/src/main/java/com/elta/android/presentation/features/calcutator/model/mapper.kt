package com.elta.android.presentation.features.calcutator.model // ktlint-disable filename

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.Portion

internal fun Dish.toUi(): DishUi =
    DishUi(
        id = id,
        name = name,
        isVerification = isVerification,
        portions = portions.map { it.toUi() },
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
