package com.elta.android.domain.features.calculator.model

data class Product(
    val foodId: String,
    val foodName: String,
    val servingId: String,
    val metricServingAmount: Double,
    val metricServingLink: MetricServingLink,
    val carbohydrate: Int,
    val fat: Int?,
    val calories: Int?,
    val protein: Int?
)
