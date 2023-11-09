package com.elta.android.presentation.features.calcutator.custom.model

import com.elta.android.domain.features.calculator.model.MetricServingLink

data class ProductUiEntity(
    val name: String? = null,
    val metricServingLink: MetricServingLink? = null,
    val numberOfUnits: Double? = null,
    val carbohydrate: Int? = null,
    val breadUnits: Double? = null
)
