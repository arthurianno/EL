package com.elta.android.presentation.widgets.date_picker

import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.date_picker.adapter.items.DatePickerItem
import org.joda.time.DateTime
import java.util.Date

object DatePickerDataProvider {

    private const val DAYS_OFFSET = 3
    private val dayOfWeekResources = arrayListOf(
        R.string.date_picker_day_of_week_1,
        R.string.date_picker_day_of_week_2,
        R.string.date_picker_day_of_week_3,
        R.string.date_picker_day_of_week_4,
        R.string.date_picker_day_of_week_5,
        R.string.date_picker_day_of_week_6,
        R.string.date_picker_day_of_week_7
    )

    fun buildDatePickerDates(currentDate: Date): ArrayList<DatePickerItem> {
        val dates = arrayListOf<DatePickerItem>()

        val selectedDate = DateTime(currentDate).withTimeAtStartOfDay()
        val todayDate = DateTime().withTimeAtStartOfDay()

        var tempDate = DateTime(selectedDate)

        val firstDayOfMonth = DateTime(selectedDate).dayOfMonth().withMinimumValue()
        val lastDayOfMonth = DateTime(selectedDate).dayOfMonth().withMaximumValue()

        dates.add(tempDate.toItem())

        if (tempDate.isAfter(firstDayOfMonth)) {
            do {
                tempDate = tempDate.minusDays(1)
                dates.add(0, tempDate.toItem())
            } while (tempDate.isAfter(firstDayOfMonth))
        }

        var inFutureDateStart: DateTime

        if (selectedDate.isBefore(todayDate) && selectedDate.isBefore(lastDayOfMonth)) {

            val maxAvailableDate = if (todayDate.isBefore(lastDayOfMonth)) todayDate else lastDayOfMonth
            tempDate = DateTime(selectedDate)

            do {
                tempDate = tempDate.plusDays(1)
                dates.add(tempDate.toItem())
            } while (tempDate.isBefore(maxAvailableDate))

            inFutureDateStart = DateTime(maxAvailableDate)
        } else {
            inFutureDateStart = DateTime(selectedDate)
        }

        var inPastDateStart = DateTime(firstDayOfMonth)

        for (i in 1..DAYS_OFFSET) {
            inPastDateStart = inPastDateStart.minusDays(1)
            dates.add(0, inPastDateStart.toItem(false))

            inFutureDateStart = inFutureDateStart.plusDays(1)
            dates.add(inFutureDateStart.toItem(false))
        }
        return dates
    }

    private fun DateTime.toItem(isAvailable: Boolean = true): DatePickerItem =
        DatePickerItem(
            toDate(),
            dayOfWeek,
            dayOfMonth,
            dayOfWeekResources[dayOfWeek - 1],
            isAvailable
        )
}