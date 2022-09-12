package com.elta.android.presentation.utils

import android.graphics.PointF
import kotlin.math.pow
import kotlin.math.sqrt

infix fun PointF.distanceBetween(other: PointF): Int =
    sqrt((other.x - x).toDouble().pow(2.0) + (other.y - y).toDouble().pow(2.0))
        .toInt()
