package com.elta.android.presentation.features.calcutator.model // ktlint-disable filename

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.Serving
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.presentation.features.calcutator.viewmodel.DIGIT_DOT
import com.elta.android.presentation.features.calcutator.viewmodel.DIGIT_DOT_ALLOWED_CHAR
import com.elta.android.presentation.features.calcutator.viewmodel.DIGIT_ZERO_STRING
import com.elta.android.presentation.features.calcutator.viewmodel.EMPTY_STRING
import com.elta.android.presentation.features.calcutator.viewmodel.NOTHING_DASH
import com.elta.android.presentation.features.calcutator.viewmodel.PATTERN_ZERO_AFTER_DECIMAL
import com.elta.android.presentation.features.calcutator.viewmodel.TWO_DECIMAL_PLACES
import com.elta.android.presentation.features.calcutator.viewmodel.ZERO_COUNT
import java.text.DecimalFormat

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
        breadUnits = breadUnits.toString()
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
        servingAmount = servingAmount.toDouble(),
        breadUnits = breadUnits.toDouble(),
        isVerified = isVerified
    )

internal fun List<Dish>.toUi(): List<DishUiEntity> =
    map { it.toUi() }

private fun Serving.toUi(): ServingUiEntity =
    ServingUiEntity(
        id = id,
        servingDescription = servingDescription,
        numberOfUnits = numberOfUnits.toString(),
        calories = calories?.format().replaceEmpty(),
        protein = proteins?.format().replaceEmpty(),
        fat = fats?.format().replaceEmpty(),
        carbohydrate = carbohydrate.format()
    )

internal fun ServingUiEntity.toDomain(): Serving =
    Serving(
        id = id,
        servingDescription = servingDescription,
        numberOfUnits = numberOfUnits.toDouble(),
        calories = calories.toDoubleOrNull() ?: ZERO_COUNT,
        proteins = protein.toDoubleOrNull() ?: ZERO_COUNT,
        fats = fat.toDoubleOrNull() ?: ZERO_COUNT,
        carbohydrate = carbohydrate.toDouble()
    )

internal fun List<DishUiEntity>.toDomain(): List<Dish> =
    map { it.toDomain() }

internal fun emptyServing() = Serving.empty().toUi()

internal fun ServingUiEntity.toNewAmount(amount: Double): ServingUiEntity {
    return copy(
        calories = calories.toCalculate(amount, numberOfUnits),
        protein = protein.toCalculate(amount, numberOfUnits),
        fat = fat.toCalculate(amount, numberOfUnits),
        carbohydrate = carbohydrate.toCalculate(amount, numberOfUnits)
    )
}

fun String.toCalculate(amount: Double, numberOfUnits: String): String =
    this.toDoubleOrNull()?.toCalculate(amount, numberOfUnits.toDouble())?.format() ?: NOTHING_DASH

fun Double.toCalculate(multiplier: Double, divisor: Double): Double {
    val result = (this * multiplier) / divisor
    return if (result.isNaN()) 0.0 else result
}

private fun Dish.selectServingCalories(): Pair<String, String> = with(servingSelect) {
    when {
        servingDescription.isNotEmpty() && calories != ZERO_COUNT -> {
            "${servingAmount.format()} $servingDescription" to calories?.format().orEmpty()
        }
        servingDescription.isNotEmpty() && isVerified -> {
            "${servingAmount.format()} $servingDescription" to EMPTY_STRING
        }
        else -> {
            val firstServing = servings.firstOrNull()
            "${firstServing?.numberOfUnits?.format()} ${firstServing?.servingDescription}" to firstServing?.calories?.format().replaceZero()
        }
    }
}

private fun Double.format(): String =
    this.round(TWO_DECIMAL_PLACES).removeZero()

private fun Double.removeZero(): String {
    val format = DecimalFormat(PATTERN_ZERO_AFTER_DECIMAL)
    return format.format(this).replace(DIGIT_DOT_ALLOWED_CHAR, DIGIT_DOT)
}

private fun String?.replaceEmpty(): String =
    if (this?.toDoubleOrNull() == null) NOTHING_DASH
    else this

private fun String?.replaceZero(): String =
    if (this in listOf(DIGIT_ZERO_STRING, ZERO_COUNT.toString())) EMPTY_STRING
    else this.orEmpty()
