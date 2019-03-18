package com.elta.android.presentation.widgets.date_picker

import com.elta.android.presentation.widgets.date_picker.adapter.items.DatePickerItem
import com.nullgr.core.adapter.items.ListItem
import org.joda.time.DateTime
import java.util.Date

object DatePickerDataProvider {

    private const val FIRST_DAY = 1
    private const val DAYS_OFFSET = 3

    fun buildDatePickerDates(currentDate: Date): List<ListItem> {
        val dates = arrayListOf<DatePickerItem>()
        val today = DateTime(currentDate)
        var tempDate = DateTime(today)
        val firstDayOfMonth = DateTime(currentDate).withDayOfMonth(FIRST_DAY)

        dates.add(tempDate.toItem())

        do {
            tempDate = tempDate.minusDays(1)
            dates.add(0, tempDate.toItem())
        } while (tempDate.isAfter(firstDayOfMonth))

        var prevTempDate = DateTime(firstDayOfMonth)
        var nextTempDate = DateTime(today)

        for (i in 1..DAYS_OFFSET) {
            prevTempDate = prevTempDate.minusDays(1)
            dates.add(0, prevTempDate.toItem(false))

            nextTempDate = nextTempDate.plusDays(1)
            dates.add(nextTempDate.toItem(false))
        }
        return dates
    }

    private fun DateTime.toItem(isAvailable: Boolean = true): DatePickerItem =
        DatePickerItem(
            toDate(),
            dayOfWeek,
            dayOfMonth,
            isAvailable
        )
}