package com.elta.android.presentation.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
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