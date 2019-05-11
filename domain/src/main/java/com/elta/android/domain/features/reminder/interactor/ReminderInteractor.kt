package com.elta.android.domain.features.reminder.interactor

import com.elta.android.domain.features.reminder.model.Reminder

fun List<Reminder>.sortByTime(): List<Reminder> = sortedBy { it.time }