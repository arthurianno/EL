package com.elta.android.presentation.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import com.elta.android.presentation.R
import java.util.Calendar
import java.util.Date

fun Context?.showDatePickerDialog(
    currentDate: Date,
    minDate: Date? = null,
    maxDate: Date? = null,
    onDateSelectedFunction: (Date) -> Unit
) {
    if (this == null) return
    val c = currentDate.toCalendar()
    val onDateSelectedListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        c.set(year, month, dayOfMonth)
        onDateSelectedFunction.invoke(c.time)
    }
    DatePickerDialog(this, onDateSelectedListener, c.year, c.month, c.dayOfMonth).apply {
        minDate?.let { datePicker.minDate = it.time }
        maxDate?.let { datePicker.maxDate = it.time }
        show()
    }
}

fun Context?.showTimePickerDialog(
    currentDate: Date,
    onDateSelectedFunction: (Date) -> Unit
) {
    if (this == null) return
    val c = currentDate.toCalendar()
    val onTimeSelectedListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        c.set(c.year, c.month, c.dayOfMonth, hourOfDay, minute)
        onDateSelectedFunction.invoke(c.time)
    }
    TimePickerDialog(this, onTimeSelectedListener, c.hourOfDay, c.minute, true).show()
}

@Suppress("MagicNumber")
fun Context?.showTimePickerWithoutPastTimeDialog(
    selectedDate: Date,
    onDateSelectedFunction: (Date) -> Unit
) {
    if (this == null) return
    val c = selectedDate.toCalendar()
    val onTimeSelectedListener =
        TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val currentCalendar = Calendar.getInstance().apply {
                // It needs to prevent choose current minute on time picker.
                add(Calendar.MINUTE, 1)
            }
            val pickedCalendar = Calendar.getInstance().apply {
                set(c.year, c.month, c.dayOfMonth, hourOfDay, minute)
            }
            if (pickedCalendar.before(currentCalendar)) {
                Toast.makeText(
                    this,
                    getString(R.string.profile_reminders_wrong_time),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                onDateSelectedFunction.invoke(pickedCalendar.time)
            }
        }

    TimePickerDialog(this, onTimeSelectedListener, c.hourOfDay, c.minute, true).show()
}