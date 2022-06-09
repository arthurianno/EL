package com.elta.android.domain.features.diary.home.model

object DayPeriodHolder {

    // 11:59:59
    private const val APP_MORNING_END_H = 11

    // 12:00:00
    private const val APP_DAY_START_H = 12

    // 17:59:59
    private const val APP_DAY_END_H = 17

    val morning
        get() = LongRange(
            atStartOfDay(),
            atEndOhHour(APP_MORNING_END_H)
        )

    val day
        get() = LongRange(
            atHour(APP_DAY_START_H),
            atEndOhHour(APP_DAY_END_H)
        )
}
