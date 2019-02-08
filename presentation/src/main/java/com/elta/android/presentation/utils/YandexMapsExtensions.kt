package com.elta.android.presentation.utils

import android.location.Location
import com.yandex.mapkit.geometry.Point

fun Location.toPoint(): Point = Point(this.latitude, this.longitude)

fun Point.asString() = "$latitude / $longitude"