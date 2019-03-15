package com.elta.android.presentation.widgets.date_picker.adapter.items

import java.util.Date

data class DatePickerItem(
    val date: Date,
    val isAvailable: Boolean = true
)