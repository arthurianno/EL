package com.elta.android.presentation.features.calcutator.mappers

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.presentation.features.calcutator.products.viewmodel.DIGIT_DOT
import com.elta.android.presentation.features.calcutator.products.viewmodel.DIGIT_DOT_ALLOWED_CHAR
import com.elta.android.presentation.features.calcutator.products.viewmodel.DIGIT_ZERO_STRING
import com.elta.android.presentation.features.calcutator.products.viewmodel.EMPTY_STRING
import com.elta.android.presentation.features.calcutator.products.viewmodel.NOTHING_DASH
import com.elta.android.presentation.features.calcutator.products.viewmodel.PATTERN_ZERO_AFTER_DECIMAL
import com.elta.android.presentation.features.calcutator.products.viewmodel.TWO_DECIMAL_PLACES
import com.elta.android.presentation.features.calcutator.products.viewmodel.ZERO_COUNT
import java.text.DecimalFormat

internal fun String.toCalculate(amount: Double, numberOfUnits: String): String =
    this.toDoubleOrNull()?.toCalculate(amount, numberOfUnits.toDouble())?.format() ?: NOTHING_DASH

internal fun Double.toCalculate(multiplier: Double, divisor: Double): Double {
    val result = (this * multiplier) / divisor
    return if (result.isNaN()) 0.0 else result
}

internal fun Double.format(): String =
    this.round(TWO_DECIMAL_PLACES).removeZero()

private fun Double.removeZero(): String {
    val format = DecimalFormat(PATTERN_ZERO_AFTER_DECIMAL)
    return format.format(this).replace(DIGIT_DOT_ALLOWED_CHAR, DIGIT_DOT)
}

internal fun String?.replaceEmpty(): String =
    if (this?.toDoubleOrNull() == null) NOTHING_DASH
    else this

internal fun String?.replaceZero(): String =
    if (this in listOf(DIGIT_ZERO_STRING, ZERO_COUNT.toString())) EMPTY_STRING
    else this.orEmpty()

internal fun calculateBreadUnits(carbohydrate: Double): Double {
    return (carbohydrate / CONVERSION_FACTOR).round(ONE_DECIMAL_PLACE)
}

internal fun Dish.selectServingCalories(): Pair<String, String> = with(servingSelect) {
    when {
        metricServingLink.name.isNotEmpty() && breadUnits != ZERO_COUNT -> {
            "${servingAmount.format()} ${metricServingLink.name}" to breadUnits.format()
        }

        metricServingLink.name.isNotEmpty() && isVerified -> {
            "${servingAmount.format()} ${metricServingLink.name}" to EMPTY_STRING
        }

        else -> {
            val firstServing = servings.firstOrNull()
            val breadUnits =
                firstServing?.let { calculateBreadUnits(firstServing.carbohydrate) }?.format()
                    .replaceZero()
            "${firstServing?.numberOfUnits?.format()} ${firstServing?.metricServingLink?.name}" to breadUnits
        }
    }
}

internal fun Double?.notNullOrZero(): Boolean = this != null && this != 0.0
internal fun Int?.notNullOrZero(): Boolean = this != null && this != 0

const val ONE_DECIMAL_PLACE = 1
const val CONVERSION_FACTOR = 10

