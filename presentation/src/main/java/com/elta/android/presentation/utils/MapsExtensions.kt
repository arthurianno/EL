package com.elta.android.presentation.utils

import android.location.Location
import com.elta.android.domain.features.sale_points.model.Coordinates
import com.elta.android.presentation.R
import com.elta.android.presentation.core.geo.GeoPoint
import com.nullgr.core.resources.ResourceProvider
import com.yandex.mapkit.geometry.Point

fun Location.toPoint(): Point = Point(this.latitude, this.longitude)

fun Point.asString() = "$latitude / $longitude"

fun GeoPoint.toPoint(): Point = Point(latitude, longitude)

fun Location.distanceTo(coordinates: Coordinates): Float {
    Location.distanceBetween(
        this.latitude,
        this.longitude,
        coordinates.latitude,
        coordinates.longitude,
        distanceResultHolder
    )
    return distanceResultHolder[0]
}

@Suppress("MagicNumber")
val moskowLocation = Location("").apply {
    latitude = 55.753638
    longitude = 37.621913
}

fun Float?.formatDistance(resources: ResourceProvider): String {
    return when {
        this == null -> ""
        this < KILOMETER -> resources.getString(R.string.shops_map_distance_m_pattern, this.toInt())
        else -> resources.getString(R.string.shops_map_distance_km_pattern, (this / KILOMETER).toInt())
    }
}

private val distanceResultHolder = FloatArray(1)
private const val KILOMETER = 1000