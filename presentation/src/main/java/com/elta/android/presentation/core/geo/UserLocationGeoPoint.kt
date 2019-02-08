package com.elta.android.presentation.core.geo

import android.location.Location

data class UserLocationGeoPoint(
    val location: Location
) : GeoPoint(location.latitude, location.longitude)