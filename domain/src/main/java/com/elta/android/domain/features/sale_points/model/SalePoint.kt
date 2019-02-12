package com.elta.android.domain.features.sale_points.model

data class SalePoint(
    val id: String,
    val name: String,
    val type: Type,
    val region: String,
    val city: String,
    val address: String,
    val phone: String?,
    val coordinates: Coordinates,
    val timeStamp: Int,
    var distance: Float? = null
)