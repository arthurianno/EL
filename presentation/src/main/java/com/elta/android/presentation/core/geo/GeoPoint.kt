package com.elta.android.presentation.core.geo

import com.yandex.mapkit.geometry.Point

@Suppress("MagicNumber")
open class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val id: Int? = null,
    var selected: Boolean? = false,
    var meta: Any? = null
) {

    fun toPoint() = Point(latitude, longitude)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeoPoint

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + (id ?: 0)
        return result
    }
}