package com.elta.android.presentation.features.calcutator.mappers

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.presentation.features.calcutator.custom.model.CreateCustomProductFlow
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.products.viewmodel.DIGIT_DOT
import com.elta.android.presentation.features.calcutator.products.viewmodel.DIGIT_DOT_ALLOWED_CHAR
import com.elta.android.presentation.features.calcutator.products.viewmodel.DIGIT_ZERO_STRING
import com.elta.android.presentation.features.calcutator.products.viewmodel.EMPTY_STRING
import com.elta.android.presentation.features.calcutator.products.viewmodel.PATTERN_ZERO_AFTER_DECIMAL
import com.elta.android.presentation.features.calcutator.products.viewmodel.TWO_DECIMAL_PLACES
import java.text.DecimalFormat
import kotlin.math.roundToInt

internal fun String.toCalculate(amount: Double, numberOfUnits: String): String {
    val value = this.toDoubleOrNull() ?: return ""
    return value
        .toCalculate(amount, numberOfUnits.toDouble())
        .roundToInt()
        .toString()
}

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

internal fun String?.replaceZero(): String =
    if (this in listOf(DIGIT_ZERO_STRING, ZERO_COUNT_DOUBLE.toString())) EMPTY_STRING
    else this.orEmpty()

internal fun calculateBreadUnits(carbohydrate: Double): Double =
    (carbohydrate / CONVERSION_FACTOR).round(ONE_DECIMAL_PLACE)

internal fun Dish.selectServingCalories(): Pair<String, String> = with(servingSelect) {
    when {
        metricServingLink.name.isNotEmpty() && breadUnits != ZERO_COUNT_DOUBLE -> {
            "${servingAmount.format()} ${metricServingLink.name}" to breadUnits?.format().orEmpty()
        }

        metricServingLink.name.isNotEmpty() && isVerified -> {
            "${servingAmount.format()} ${metricServingLink.name}" to EMPTY_STRING
        }

        else -> {
            val firstServing = servings.firstOrNull()
            val breadUnits =
                firstServing?.let { firstServing.carbohydrate?.let { it1 -> calculateBreadUnits(it1) } }
                    ?.format()
                    .replaceZero()
            "${firstServing?.numberOfUnits?.format()} ${firstServing?.metricServingLink?.name}" to breadUnits
        }
    }
}

internal fun getCreateCustomProductFlow(calculatorFlow: CalculatorFlow, dish: DishUiEntity?) =
    when {
        dish == null -> CreateCustomProductFlow.CREATING
        calculatorFlow == CalculatorFlow.BREAD_UNITS && !dish.servingSelect.isCarbohydrateValid() -> CreateCustomProductFlow.EDITING
        else -> CreateCustomProductFlow.VIEWING
    }

internal fun DishUiEntity.addFirstServing() = this.copy(servingSelect = this.servings.first())

internal fun Double?.notNullOrZero(): Boolean = this != null && this != ZERO_COUNT_DOUBLE
internal fun Int?.notNullOrZero(): Boolean = this != null && this != ZERO_COUNT_INT

fun Double?.breadUnitsIsMax(): Boolean = (this ?: ZERO_COUNT_DOUBLE) > MAX_BREAD_UNITS

const val ONE_DECIMAL_PLACE = 1
private const val CONVERSION_FACTOR = 10
private const val MAX_BREAD_UNITS = 99.9
internal const val ZERO_COUNT_DOUBLE = 0.0
internal const val ZERO_COUNT_INT = 0

