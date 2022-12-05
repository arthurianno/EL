package com.elta.android.data.features.diary.events.extensions // ktlint-disable filename

fun List<*>?.countOrZero(): Long =
    this?.count()?.toLong() ?: 0
