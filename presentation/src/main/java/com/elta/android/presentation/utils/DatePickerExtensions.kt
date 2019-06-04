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
    currentDate: ZonedDateTime,
    minDate: ZonedDateTime? = null,
    maxDate: ZonedDateTime? = null,
    onDateSelectedFunction: (ZonedDateTime) -> Unit
) {
    if (this == null) return
    val onDateSelectedListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        onDateSelectedFunction.invoke(currentDate.with(LocalDate.of(year, month, dayOfMonth)))
    }
    DatePickerDialog(this, onDateSelectedListener, currentDate.year, currentDate.monthValue, currentDate.dayOfMonth).apply {
        minDate?.let { datePicker.minDate = it.toMillis() }
        maxDate?.let { datePicker.maxDate = it.toMillis() }
        show()
    }
}

fun Context?.showDatePickerDialog(
    currentDate: LocalDate,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    onDateSelectedFunction: (LocalDate) -> Unit
) {
    if (this == null) return
    val onDateSelectedListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        onDateSelectedFunction.invoke(LocalDate.of(year, month, dayOfMonth))
    }
    DatePickerDialog(this, onDateSelectedListener, currentDate.year, currentDate.monthValue, currentDate.dayOfMonth).apply {
        minDate?.let { datePicker.minDate = it.toMillis() }
        maxDate?.let { datePicker.maxDate = it.toMillis() }
        show()
    }
}

fun Context?.showTimePickerDialog(
    currentDate: ZonedDateTime,
    onDateSelectedFunction: (ZonedDateTime) -> Unit
) {
    if (this == null) return
    val onTimeSelectedListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        onDateSelectedFunction.invoke(currentDate.with(LocalTime.of(hourOfDay, minute)))
    }
    TimePickerDialog(this, onTimeSelectedListener, currentDate.hour, currentDate.minute, true).show()
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
            val currentDate = ZonedDateTime.now().plusMinutes(1)
            val pickerDate = ZonedDateTime.from(selectedDate).with(LocalTime.of(hourOfDay, minute))

            if (pickerDate.isBefore(currentDate)) {
                Toast.makeText(
                    this,
                    getString(R.string.profile_reminders_wrong_time),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                onDateSelectedFunction.invoke(pickerDate)
            }
        }

    TimePickerDialog(this, onTimeSelectedListener, selectedDate.hour, selectedDate.minute, true).show()
}