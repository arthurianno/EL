package com.elta.android.domain

import com.elta.android.domain.features.reminder.interactor.getNextReminderDate
import com.elta.android.domain.features.reminder.model.ScheduleType
import org.junit.Test
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoField

class ReminderInteractorTest {

    @Test
    fun getNextReminderDate_ScheduleTypeNONE_null() {
        val type = ScheduleType.NONE
        val reminderDate = ZonedDateTime.of(2019, 6, 6, 15, 23, 0, 0, ZoneId.systemDefault())
        val nextDate = getNextReminderDate(type = type, reminderDate = reminderDate)
        assert(nextDate == null)
    }

    @Test
    fun getNextReminderDate_ScheduleTypeDay_nextDay() {
        val type = ScheduleType.DAY
        val reminderDate = ZonedDateTime.of(2019, 6, 6, 15, 23, 0, 0, ZoneId.systemDefault())
        val nextDate = getNextReminderDate(type = type, reminderDate = reminderDate)
        val nextExpectedDate = reminderDate.with(ChronoField.DAY_OF_MONTH, 7)
        assert(nextDate == nextExpectedDate)
    }

    @Test
    fun getNextReminderDate_ScheduleTypeWEEK_nextWeek() {
        val type = ScheduleType.WEEK
        val reminderDate = ZonedDateTime.of(2019, 6, 6, 15, 23, 0, 0, ZoneId.systemDefault())
        val nextDate = getNextReminderDate(type = type, reminderDate = reminderDate)
        val nextExpectedDate = reminderDate.with(ChronoField.DAY_OF_MONTH, 13)
        assert(nextDate == nextExpectedDate)
    }

    @Test
    fun getNextReminderDate_ScheduleTypeMONTH_nextMonth() {
        val type = ScheduleType.MONTH
        val reminderDate = ZonedDateTime.of(2019, 6, 6, 15, 23, 0, 0, ZoneId.systemDefault())
        val nextDate = getNextReminderDate(type = type, reminderDate = reminderDate)
        val nextExpectedDate = reminderDate.with(ChronoField.MONTH_OF_YEAR, 7)
        assert(nextDate == nextExpectedDate)
    }

    @Test
    fun getNextReminderDate_ScheduleTypeYEAR_nextYear() {
        val type = ScheduleType.YEAR
        val reminderDate = ZonedDateTime.of(2019, 6, 6, 15, 23, 0, 0, ZoneId.systemDefault())
        val nextDate = getNextReminderDate(type = type, reminderDate = reminderDate)
        val nextExpectedDate = reminderDate.with(ChronoField.YEAR, 2020)
        assert(nextDate == nextExpectedDate)
    }
}