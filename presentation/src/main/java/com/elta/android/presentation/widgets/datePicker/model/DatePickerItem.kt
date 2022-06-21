package com.elta.android.presentation.widgets.datePicker.model

import com.nullgr.core.adapter.items.ListItem
import org.threeten.bp.LocalDate

data class DatePickerItem(
    val date: LocalDate,
    val dayOfWeek: Int,
    val dayOfMonth: Int,
    val dayOfWeekResId: Int,
    val isAvailable: Boolean = true
) : ListItem
