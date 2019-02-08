package com.elta.android.presentation.core.geo

@Suppress("MagicNumber")
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val id: Any? = null,
    var selected: Boolean = false,
    val isUserPoint: Boolean = false,
    var meta: Any? = null
)