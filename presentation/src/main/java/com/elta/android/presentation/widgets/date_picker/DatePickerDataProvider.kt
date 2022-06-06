package com.elta.android.presentation.widgets.date_picker

import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.date_picker.adapter.items.DatePickerItem
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

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

    fun buildDatePickerDates(currentDate: LocalDate): ArrayList<DatePickerItem> {
        val dates = arrayListOf<DatePickerItem>()

        val selectedDate = currentDate
        val todayDate = LocalDate.now()

        var tempDate = LocalDate.from(selectedDate)

        val firstDayOfMonth = YearMonth.from(selectedDate).atDay(1)
        val lastDayOfMonth = YearMonth.from(selectedDate).atEndOfMonth()

        dates.add(tempDate.toItem())

        if (tempDate.isAfter(firstDayOfMonth)) {
            do {
                tempDate = tempDate.minusDays(1)
                dates.add(0, tempDate.toItem())
            } while (tempDate.isAfter(firstDayOfMonth))
        }

        var inFutureDateStart: LocalDate

        if (selectedDate.isBefore(todayDate) && selectedDate.isBefore(lastDayOfMonth)) {

            val maxAvailableDate =
                if (todayDate.isBefore(lastDayOfMonth)) todayDate else lastDayOfMonth
            tempDate = LocalDate.from(selectedDate)

            do {
                tempDate = tempDate.plusDays(1)
                dates.add(tempDate.toItem())
            } while (tempDate.isBefore(maxAvailableDate))

            inFutureDateStart = LocalDate.from(maxAvailableDate)
        } else {
            inFutureDateStart = LocalDate.from(selectedDate)
        }

        var inPastDateStart = LocalDate.from(firstDayOfMonth)

        for (i in 1..DAYS_OFFSET) {
            inPastDateStart = inPastDateStart.minusDays(1)
            dates.add(0, inPastDateStart.toItem(false))

            inFutureDateStart = inFutureDateStart.plusDays(1)
            dates.add(inFutureDateStart.toItem(false))
        }
        return dates
    }

    private fun LocalDate.toItem(isAvailable: Boolean = true): DatePickerItem =
        DatePickerItem(
            this,
            dayOfWeek.value,
            dayOfMonth,
            dayOfWeekResources[dayOfWeek.value - 1],
            isAvailable
        )
}
