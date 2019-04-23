package com.elta.android.presentation.utils

import android.graphics.PointF

infix fun PointF.distanceBetween(other: PointF): Int =
    Math.sqrt(Math.pow((other.x - x).toDouble(), 2.0) + Math.pow((other.y - y).toDouble(), 2.0)).toInt()