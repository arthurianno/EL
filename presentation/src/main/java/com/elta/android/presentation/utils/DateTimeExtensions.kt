package com.elta.android.presentation.utils

import com.elta.android.presentation.R
import com.nullgr.core.resources.ResourceProvider
import java.util.Calendar

fun Calendar.getGreetingText(resource: ResourceProvider): String =
    resource.getString(
        when (this.get(Calendar.HOUR_OF_DAY)) {
            in MORNING_START_TIME until MORNING_END_TIME ->
                R.string.main_records_new_day_title_morning
            in AFTERNOON_START_TIME until AFTERNOON_END_TIME ->
                R.string.main_records_new_day_title_afternoon
            in EVENING_START_TIME until EVENING_END_TIME ->
                R.string.main_records_new_day_title_evening
            else -> R.string.main_records_firs_launch_title
        }
    )

private const val MORNING_START_TIME = 0
private const val MORNING_END_TIME = 12
private const val AFTERNOON_START_TIME = 12
private const val AFTERNOON_END_TIME = 18
private const val EVENING_START_TIME = 18
private const val EVENING_END_TIME = 24