package com.elta.android.presentation.features.main.events.mapper // ktlint-disable filename

import com.elta.android.domain.features.user.interactor.round
import java.util.concurrent.TimeUnit

internal fun Long?.toPickerValues(): Pair<Int, Int> {
    if (this == null) return 0 to 0
    val hours = TimeUnit.SECONDS.toHours(this)
    val minutes = TimeUnit.SECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(hours)
    return hours.toInt() to minutes.toInt()
}

internal fun Double?.toPickerValues(): Pair<Int, Int> {
    if (this == null) return 0 to 0
    val tokens = this
        .round(1)
        .toString().split(".")
    val left = tokens[0].toInt()
    val right = tokens[1].toInt()
    return left to right
}
