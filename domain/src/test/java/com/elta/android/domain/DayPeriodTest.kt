package com.elta.android.domain

import com.elta.android.domain.features.diary.home.interactor.getDayPeriod
import com.elta.android.domain.features.diary.home.model.DayPeriod
import com.elta.android.domain.features.diary.home.model.atEndOhHour
import com.elta.android.domain.features.diary.home.model.atHalfPastHour
import com.elta.android.domain.features.diary.home.model.atHour
import com.elta.android.domain.features.diary.home.model.atStartOfDay
import org.junit.Test

class DayPeriodTest {

    // morning
    @Test
    fun getDayPeriod_00_00_00_Morning() {
        val now = atStartOfDay()
        val period = getDayPeriod(now)
        assert(period == DayPeriod.MORNING)
    }

    @Test
    fun getDayPeriod_06_00_00_Morning() {
        val now = atHour(6)
        val period = getDayPeriod(now)
        assert(period == DayPeriod.MORNING)
    }

    @Test
    fun getDayPeriod_11_59_59_Morning() {
        val now = atEndOhHour(11)
        val period = getDayPeriod(now)
        assert(period == DayPeriod.MORNING)
    }

    // afternoon
    @Test
    fun getDayPeriod_12_00_00_Afternoon() {
        val now = atHour(12)
        val period = getDayPeriod(now)
        assert(period == DayPeriod.AFTERNOON)
    }

    @Test
    fun getDayPeriod_12_30_00_Afternoon() {
        val now = atHalfPastHour(12)
        val period = getDayPeriod(now)
        assert(period == DayPeriod.AFTERNOON)
    }

    @Test
    fun getDayPeriod_17_59_59_Afternoon() {
        val now = atEndOhHour(17)
        val period = getDayPeriod(now)
        assert(period == DayPeriod.AFTERNOON)
    }

    // evening
    @Test
    fun getDayPeriod_18_00_00_Evening() {
        val now = atHour(18)
        val period = getDayPeriod(now)
        assert(period == DayPeriod.EVENING)
    }

    @Test
    fun getDayPeriod_18_30_00_Evening() {
        val now = atHalfPastHour(18)
        val period = getDayPeriod(now)
        assert(period == DayPeriod.EVENING)
    }

    @Test
    fun getDayPeriod_23_59_59_Evening() {
        val now = atEndOhHour(23)
        val period = getDayPeriod(now)
        assert(period == DayPeriod.EVENING)
    }
}
