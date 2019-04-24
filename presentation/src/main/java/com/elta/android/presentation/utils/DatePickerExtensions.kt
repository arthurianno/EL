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

@Suppress("MagicNumber")
fun Context?.showTimePickerDialog(selectedDate: Date, onDateSelectedFunction: (Date) -> Unit) {
    if (this == null) return
    val c = selectedDate.toCalendar()
    val onTimeSelectedListener =
        TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val currentCalendar = Calendar.getInstance().apply {
                // It needs to prevent choose current minute on time picker.
                add(Calendar.MINUTE, 1)
            }
            val pickedCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
            }
            if (pickedCalendar.before(currentCalendar)) {
                Toast.makeText(
                    this,
                    getString(R.string.profile_reminders_wrong_time),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                c.set(Calendar.MINUTE, minute)
                onDateSelectedFunction.invoke(c.time)
            }
        }

    TimePickerDialog(
        this,
        onTimeSelectedListener,
        c.hourOfDay,
        c.minute,
        true
    )
        .show()
}