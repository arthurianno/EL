package com.elta.android.domain.features.diary.home.model

import java.util.Calendar
import java.util.Date

const val GENERAL_DAY_END_H = 23
const val GENERAL_END_M = 59
const val GENERAL_END_S = 59

fun Date.atEndOfDay(): Date = atTimeOfDay(GENERAL_DAY_END_H, GENERAL_END_M, GENERAL_END_S)

fun Date.atTimeOfDay(h: Int = 0, m: Int = 0, s: Int = 0): Date {
    val c = Calendar.getInstance()
    c.time = this
    c.set(Calendar.HOUR_OF_DAY, h)
    c.set(Calendar.MINUTE, m)
    c.set(Calendar.SECOND, s)
    c.set(Calendar.MILLISECOND, 0)
    return c.time
}