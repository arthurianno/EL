package com.elta.android.presentation.features.calcutator.model // ktlint-disable filename

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.Serving
import com.elta.android.domain.features.user.interactor.round

internal fun Dish.toUi(): DishUiEntity =
    DishUiEntity(
        id = id,
        localId = localId,
        name = name,
        type = type,
        brandName = brandName,
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
        breadUnits = breadUnits
    )

internal fun List<Dish>.toUi(): List<DishUiEntity> =
    map { it.toUi() }

private fun Serving.toUi(): ServingUiEntity =
    ServingUiEntity(
        id = id,
        servingDescription = servingDescription,
        numberOfUnits = numberOfUnits,
        calories = calories,
        protein = proteins,
        fat = fats,
        carbohydrate = carbohydrate
    )

internal fun ServingUiEntity.toDomain(): Serving =
    Serving(
        id = id,
        servingDescription = servingDescription,
        numberOfUnits = numberOfUnits,
        calories = calories,
        proteins = protein,
        fats = fat,
        carbohydrate = carbohydrate
    )

internal fun List<DishUiEntity>.toDomain(): List<Dish> =
    map { it.toDomain() }

internal fun emptyServing() = Serving.empty().toUi()

internal fun ServingUiEntity.toNewAmount(amount: Double): ServingUiEntity =
    this.copy(
        calories = calories * amount,
        protein = protein * amount,
        fat = fat * amount,
        carbohydrate = carbohydrate * amount
    )

internal fun ServingUiEntity.toRoundValue(count : Int = 1): ServingUiEntity =
    this.copy(
        calories = calories.round(count),
        protein = protein.round(count),
        fat = fat.round(count),
        carbohydrate = carbohydrate.round(count)
    )
