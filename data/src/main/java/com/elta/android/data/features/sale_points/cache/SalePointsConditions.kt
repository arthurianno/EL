package com.elta.android.data.features.sale_points.cache

sealed class SalePointsConditions : Condition {

    data class Query(val query: String) : SalePointsConditions()

    data class Bounds(
        val southWestLatitude: Double,
        val southWestLongitude: Double,
        val northEastLatitude: Double,
        val northEastLongitude: Double
    ) : SalePointsConditions()

    object All : SalePointsConditions()
}