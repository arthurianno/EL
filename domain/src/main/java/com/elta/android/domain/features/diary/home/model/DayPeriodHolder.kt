package com.elta.android.domain.features.diary.home.model

import java.util.Date

object DayPeriodHolder {

    // 11:59:59
    const val APP_MORNING_END_H = 11
    const val APP_MORNING_END_M = GENERAL_END_M
    const val APP_MORNING_END_S = GENERAL_END_S

    // 12:00:00
    const val APP_DAY_START_H = 12
    const val APP_DAY_START_M = 0
    const val APP_DAY_START_S = 0

    // 17:59:59
    const val APP_DAY_END_H = 17
    const val APP_DAY_END_M = GENERAL_END_M
    const val APP_DAY_END_S = GENERAL_END_S

    val morning
        get() = LongRange(
            Date().atTimeOfDay().time,
            Date().atTimeOfDay(APP_MORNING_END_H, APP_MORNING_END_M, APP_MORNING_END_S).time
        )

    val day
        get() = LongRange(
            Date().atTimeOfDay(APP_DAY_START_H, APP_DAY_START_M, APP_DAY_START_S).time,
            Date().atTimeOfDay(APP_DAY_END_H, APP_DAY_END_M, APP_DAY_END_S).time
        )
}