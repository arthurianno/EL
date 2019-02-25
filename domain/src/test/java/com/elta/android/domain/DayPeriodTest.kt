package com.elta.android.domain

import com.elta.android.domain.features.diary.home.interactor.getDayPeriod
import com.elta.android.domain.features.diary.home.model.DayPeriod
import com.elta.android.domain.features.diary.home.model.atTimeOfDay
import org.junit.Test
import java.util.Date

class DayPeriodTest {

    // morning
    @Test
    fun getDayPeriod_00_00_00_Morning() {
        val now = Date().atTimeOfDay().time
        val period = getDayPeriod(now)
        assert(period == DayPeriod.MORNING)
    }

    @Test
    fun getDayPeriod_06_00_00_Morning() {
        val now = Date().atTimeOfDay(6).time
        val period = getDayPeriod(now)
        assert(period == DayPeriod.MORNING)
    }

    @Test
    fun getDayPeriod_11_59_59_Morning() {
        val now = Date().atTimeOfDay(11, 59, 59).time
        val period = getDayPeriod(now)
        assert(period == DayPeriod.MORNING)
    }

    // day
    @Test
    fun getDayPeriod_12_00_00_Day() {
        val now = Date().atTimeOfDay(12, 0).time
        val period = getDayPeriod(now)
        assert(period == DayPeriod.DAY)
    }

    @Test
    fun getDayPeriod_12_30_00_Day() {
        val now = Date().atTimeOfDay(12, 30).time
        val period = getDayPeriod(now)
        assert(period == DayPeriod.DAY)
    }

    @Test
    fun getDayPeriod_17_59_59_Day() {
        val now = Date().atTimeOfDay(17, 59, 59).time
        val period = getDayPeriod(now)
        assert(period == DayPeriod.DAY)
    }

    // evening
    @Test
    fun getDayPeriod_18_00_00_Evening() {
        val now = Date().atTimeOfDay(18, 0).time
        val period = getDayPeriod(now)
        assert(period == DayPeriod.EVENING)
    }

    @Test
    fun getDayPeriod_18_30_00_Evening() {
        val now = Date().atTimeOfDay(18, 30).time
        val period = getDayPeriod(now)
        assert(period == DayPeriod.EVENING)
    }

    @Test
    fun getDayPeriod_23_59_59_Evening() {
        val now = Date().atTimeOfDay(23, 59, 59).time
        val period = getDayPeriod(now)
        assert(period == DayPeriod.EVENING)
    }
}