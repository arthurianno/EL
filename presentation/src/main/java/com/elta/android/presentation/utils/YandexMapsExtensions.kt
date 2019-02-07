package com.elta.android.presentation.utils

import android.location.Location
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map

fun Location.toPoint(): Point = Point(this.latitude, this.longitude)