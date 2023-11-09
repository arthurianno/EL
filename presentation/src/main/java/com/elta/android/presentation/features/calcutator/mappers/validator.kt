package com.elta.android.presentation.features.calcutator.mappers

import com.elta.android.presentation.features.calcutator.custom.model.ProductUiEntity

internal fun ProductUiEntity.isValid(): Boolean = name?.isNotBlank() == true &&
        breadUnits.notNullOrZero() &&
        numberOfUnits.notNullOrZero() &&
        carbohydrate.notNullOrZero() &&
        metricServingLink != null