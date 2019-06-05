package com.elta.android.presentation.core.geo

import com.a65apps.clustering.core.DefaultCluster
import com.a65apps.clustering.core.LatLng

@Suppress("MagicNumber", "UseDataClass")
class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val id: Any? = null,
    var selected: Boolean = false,
    val isUserPoint: Boolean = false,
    val icon: GeoPointIcon? = null,
    var meta: Any? = null
) : DefaultCluster(LatLng(latitude, longitude), id) {

    override fun isCluster(): Boolean = size() > CLUSTER_SIZE

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
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "GeoPoint(" +
            "latitude=$latitude, " +
            "longitude=$longitude, " +
            "id=$id, " +
            "selected=$selected, " +
            "isUserPoint=$isUserPoint, " +
            "icon=$icon, " +
            "meta=$meta" +
            ")"
    }

    private companion object {
        private const val CLUSTER_SIZE = 2
    }
}

private const val STUB_ID = "stub_geo_point_id"

val emptyGeoPoint = GeoPoint(0.0, 0.0, STUB_ID)

fun GeoPoint.isEmpty(): Boolean = this == emptyGeoPoint