package com.elta.android.presentation.utils

import android.location.Location
import com.elta.android.presentation.core.geo.GeoPoint
import com.yandex.mapkit.geometry.Point

fun Location.toPoint(): Point = Point(this.latitude, this.longitude)

fun Point.asString() = "$latitude / $longitude"

fun GeoPoint.toPoint(): Point = Point(latitude, longitude)