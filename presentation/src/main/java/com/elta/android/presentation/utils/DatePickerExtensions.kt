package com.elta.android.presentation.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import com.elta.android.common.utils.toMillis
import com.elta.android.presentation.R
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZonedDateTime

fun Context?.showDatePickerDialog(
    date: ZonedDateTime,
    minDate: ZonedDateTime? = null,
    maxDate: ZonedDateTime? = null,
    onDateSelectedFunction: (ZonedDateTime) -> Unit
) {
    if (this == null) return
    val onDateSelectedListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        onDateSelectedFunction.invoke(date.with(LocalDate.of(year, month + 1, dayOfMonth)))
    }
    DatePickerDialog(
        this,
        onDateSelectedListener,
        date.year,
        date.month.ordinal,
        date.dayOfMonth
    ).apply {
        minDate?.let { datePicker.minDate = it.toMillis() }
        maxDate?.let { datePicker.maxDate = it.toMillis() }
        show()
    }
}

fun Context?.showDatePickerDialog(
    date: LocalDate,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    onDateSelectedFunction: (LocalDate) -> Unit
) {
    if (this == null) return
    val onDateSelectedListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        onDateSelectedFunction.invoke(LocalDate.of(year, month + 1, dayOfMonth))
    }
    DatePickerDialog(
        this,
        onDateSelectedListener,
        date.year,
        date.month.ordinal,
        date.dayOfMonth
    ).apply {
        minDate?.let { datePicker.minDate = it.toMillis() }
        maxDate?.let { datePicker.maxDate = it.toMillis() }
        show()
    }
}

fun Context?.showTimePickerDialog(
    date: ZonedDateTime,
    onDateSelectedFunction: (ZonedDateTime) -> Unit
) {
    if (this == null) return
    val onTimeSelectedListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        onDateSelectedFunction.invoke(date.with(LocalTime.of(hourOfDay, minute)))
    }
    TimePickerDialog(this, onTimeSelectedListener, date.hour, date.minute, true).show()
}

@Suppress("MagicNumber")
fun Context?.showTimePickerWithoutPastTimeDialog(
    selectedDate: ZonedDateTime,
    onDateSelectedFunction: (ZonedDateTime) -> Unit
) {
    if (this == null) return
    val onTimeSelectedListener =
        TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            // It needs to prevent choose current minute on time picker.
            val date = ZonedDateTime.now().withSecond(0)
            val pickerDate = ZonedDateTime.from(selectedDate).with(LocalTime.of(hourOfDay, minute))

            if (pickerDate.isBefore(date)) {
                Toast.makeText(
                    this,
                    getString(R.string.profile_reminders_wrong_time),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                onDateSelectedFunction.invoke(pickerDate)
            }
        }

    TimePickerDialog(
        this,
        onTimeSelectedListener,
        selectedDate.hour,
        selectedDate.minute,
        true
    ).show()
}
