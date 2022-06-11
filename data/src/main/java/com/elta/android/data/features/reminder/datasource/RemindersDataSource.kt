package com.elta.android.data.features.reminder.datasource

import com.elta.android.data.features.reminder.dto.ReminderDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface RemindersDataSource {

    fun getReminders(): Observable<List<ReminderDto>>

    fun getReminderById(id: String): Single<ReminderDto>

    fun addReminders(reminders: List<ReminderDto>): Completable

    fun updateReminders(reminders: List<ReminderDto>): Completable

    fun deleteReminders(reminders: List<ReminderDto>): Completable
}
