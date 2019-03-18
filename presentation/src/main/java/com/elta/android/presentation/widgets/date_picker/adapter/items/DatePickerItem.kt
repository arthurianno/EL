package com.elta.android.presentation.widgets.date_picker.adapter.items

import com.nullgr.core.adapter.items.ListItem
import java.util.Date

data class DatePickerItem(
    val date: Date,
    val dayOfWeek: Int,
    val dayOfMonth: Int,
    val isAvailable: Boolean = true
) : ListItem