package com.elta.android.domain.features.reminder.repository

import com.elta.android.domain.features.reminder.model.Reminder
import io.reactivex.Observable
import io.reactivex.Single

interface RemindersRepository {

    fun getReminders(): Observable<List<Reminder>>

    fun getReminderById(id: String): Single<Reminder>

    fun addReminder(reminder: Reminder): Single<String>

    fun updateReminder(reminder: Reminder): Single<String>

    fun deleteReminder(reminder: Reminder): Single<String>
}