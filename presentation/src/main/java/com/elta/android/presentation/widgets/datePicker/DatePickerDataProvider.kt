package com.elta.android.presentation.widgets.datePicker

import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.datePicker.model.DatePickerItem
import org.threeten.bp.LocalDate

object DatePickerDataProvider {

    private const val DAYS_OFFSET = 3
    private val dayOfWeekResources = listOf(
        R.string.date_picker_day_of_week_1,
        R.string.date_picker_day_of_week_2,
        R.string.date_picker_day_of_week_3,
        R.string.date_picker_day_of_week_4,
        R.string.date_picker_day_of_week_5,
        R.string.date_picker_day_of_week_6,
        R.string.date_picker_day_of_week_7
    )

    fun buildDatePickerDates(): List<DatePickerItem> {
        val dates = mutableListOf<DatePickerItem>()
        dates.add(0, LocalDate.now().toItem())
        val stopDate = LocalDate.now().minusYears(1)
        do {
            dates.add(0, dates[0].date.minusDays(1).toItem())
        } while (dates[0].date > stopDate)

        (1..DAYS_OFFSET).map {
            dates.add(LocalDate.now().plusDays(it.toLong()).toItem(false))
        }
        return dates
    }

    private fun LocalDate.toItem(isAvailable: Boolean = true): DatePickerItem =
        DatePickerItem(
            date = this,
            dayOfWeek = dayOfWeek.value,
            dayOfMonth = dayOfMonth,
            dayOfWeekResId = dayOfWeekResources[dayOfWeek.value - 1],
            isAvailable = isAvailable
        )
}
