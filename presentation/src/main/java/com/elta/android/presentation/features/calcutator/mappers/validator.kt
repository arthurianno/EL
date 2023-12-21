package com.elta.android.presentation.features.calcutator.mappers

import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.presentation.features.calcutator.custom.model.CreateCustomProductFlow
import com.elta.android.presentation.features.calcutator.custom.model.ProductUiEntity
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.products.model.ServingUiEntity
import timber.log.Timber

internal fun ProductUiEntity.isValid(calculatorFlow: CalculatorFlow): Boolean {
    return name?.isNotBlank() == true &&
            numberOfUnits.notNullOrZero() &&
            breadUnitIsValid(calculatorFlow) &&
            metricServingLink != null
}

internal fun ServingUiEntity.isCarbohydrateValid(): Boolean =
    carbohydrate != "0" && !carbohydrate.isNullOrEmpty()

internal fun ProductUiEntity.productHasChanged(firstMetricServingLink: String?): Boolean =
    name?.isNotBlank() == true ||
            numberOfUnits.notNullOrZero() ||
            metricServingLink?.name != firstMetricServingLink ||
            carbohydrate.notNullOrZero() ||
            calories.notNullOrZero() ||
            protein.notNullOrZero() ||
            fat.notNullOrZero()

private fun ProductUiEntity.breadUnitIsValid(calculatorFlow: CalculatorFlow): Boolean {
    return if (calculatorFlow == CalculatorFlow.BREAD_UNITS) carbohydrate.notNullOrZero()
    else true
}

