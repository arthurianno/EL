package com.elta.android.presentation.widgets.date_picker

import com.elta.android.presentation.widgets.date_picker.adapter.items.DatePickerItem
import org.joda.time.DateTime
import java.util.Date

object DatePickerDataProvider {

    private const val FIRST_DAY = 1
    private const val DAYS_OFFSET = 3

    fun buildDatePickerDates(currentDate: Date): List<DatePickerItem> {
        val dates = arrayListOf<DatePickerItem>()
        val today = DateTime(currentDate)
        var tempDate = DateTime(today)
        val firstDayOfMonth = DateTime(currentDate).withDayOfMonth(FIRST_DAY)

        dates.add(DatePickerItem(tempDate.toDate()))

        do {
            tempDate = tempDate.minusDays(1)
            dates.add(0, DatePickerItem(tempDate.toDate()))
        } while (tempDate.isAfter(firstDayOfMonth))

        var prevTempDate = DateTime(firstDayOfMonth)
        var nextTempDate = DateTime(today)

        for (i in 1..DAYS_OFFSET) {
            prevTempDate = prevTempDate.minusDays(1)
            dates.add(0, DatePickerItem(prevTempDate.toDate(), false))

            nextTempDate = nextTempDate.plusDays(1)
            dates.add(DatePickerItem(nextTempDate.toDate(), false))
        }
        return dates
    }
}