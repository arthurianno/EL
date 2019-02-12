package com.elta.android.domain.features.sale_points.model

data class CoordinatesBounds(
    val northEast: Coordinates,
    val southWest: Coordinates
) {

    fun contains(coordinates: Coordinates): Boolean {
        return southWest.latitude <= coordinates.latitude &&
            coordinates.latitude <= northEast.latitude &&
            magic(coordinates.longitude)
    }

    private fun magic(longitude: Double): Boolean {
        return if (southWest.longitude <= northEast.longitude) {
            southWest.longitude <= longitude && longitude <= northEast.longitude
        } else {
            southWest.longitude <= longitude || longitude <= northEast.longitude
        }
    }
}