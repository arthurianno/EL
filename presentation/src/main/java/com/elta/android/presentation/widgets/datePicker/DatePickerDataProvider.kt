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

    fun buildDatePickerDates(date: LocalDate): List<DatePickerItem> = createDatePickerList()
        .createListToDate(date.minusYears(1))
        .addItemsAfterStart(DAYS_OFFSET)
        .addItemsBeforeToday(DAYS_OFFSET)

    private fun createDatePickerList() = mutableListOf(LocalDate.now().toItem())

    private fun MutableList<DatePickerItem>.addItemsAfterStart(count: Int): MutableList<DatePickerItem> =
        this.apply {
            repeat(count) {
                this.add(0, this[0].date.minusDays(1).toItem(false))
            }
        }

    private fun MutableList<DatePickerItem>.addItemsBeforeToday(count: Int): MutableList<DatePickerItem> =
        this.apply {
            (1..count).map {
                this.add(LocalDate.now().plusDays(it.toLong()).toItem(false))
            }
        }

    private fun MutableList<DatePickerItem>.createListToDate(date: LocalDate): MutableList<DatePickerItem> =
        this.apply {
            while (this[0].date > date) {
                this.add(0, this[0].date.minusDays(1).toItem())
            }
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
