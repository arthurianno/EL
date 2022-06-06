package com.elta.android.presentation.core.geo

import android.location.Location

data class ExtendedLocation(
    val location: Location,
    val zoom: Float? = null
)
