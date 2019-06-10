package com.elta.android.presentation.jobs

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import com.elta.android.common.utils.log
import com.elta.android.common.utils.toMillis
import com.elta.android.domain.features.reminder.interactor.DeleteReminderUseCase
import com.elta.android.domain.features.reminder.interactor.GetRemindersUseCase
import com.elta.android.domain.features.reminder.interactor.UpdateReminderUseCase
import com.elta.android.domain.features.reminder.interactor.getNextReminder
import com.elta.android.domain.features.reminder.interactor.isInPast
import com.elta.android.domain.features.reminder.interactor.isOneTime
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.notification.NotificationSource
import com.elta.android.presentation.features.profile.settings.reminders.utils.getCancelPendingIntent
import com.elta.android.presentation.features.profile.settings.reminders.utils.getPendingIntent
import com.nullgr.core.rx.RxBus
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("MagicNumber")
@SuppressLint("CheckResult")
@Singleton
class RemindersManager @Inject constructor(
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val getRemindersUseCase: GetRemindersUseCase,
    private val notificationManager: NotificationSource,
    private val context: Context,
    private val bus: RxBus
) {

    private val manager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        bus.events<Events.BootCompleted>()
            .subscribe { scheduleReminders() }

        bus.events<Events.ReminderSpent>()
            .log("Reminder", "Events.ReminderSpent")
            .map(Events.ReminderSpent::reminder)
            .doOnNext(::showReminderNotification)
            .doOnNext(::updateReminder)
            .subscribe()
    }

    fun addReminder(reminder: Reminder) {
        Timber.tag("Reminder").d("add: $reminder")
        val pi = reminder.getPendingIntent(context)
        manager.setExact(AlarmManager.RTC_WAKEUP, reminder.date.toMillis(), pi)
    }

    fun updateReminder(reminder: Reminder) {
        Timber.tag("Reminder").d("current: $reminder")
        getNextReminder(reminder)?.let { newReminder ->
            Timber.tag("Reminder").d("updated: $newReminder")
            addReminder(newReminder)
            updateReminderInternal(newReminder)
        } ?: deleteReminderInternal(reminder)
    }

    fun cancelReminder(reminderId: String) {
        Timber.tag("Reminder").d("cancel: $reminderId")
        val pi = getCancelPendingIntent(context, reminderId)
        manager.cancel(pi)
    }

    fun cancelAll() {
        val reminders = getRemindersUseCase.execute().blockingFirst()
        reminders.forEach { reminder ->
            cancelReminder(reminder.id)
        }
    }

    fun scheduleReminders() {
        val reminders = getRemindersUseCase.execute().blockingFirst()
        reminders.forEach { reminder ->
            if (reminder.isOneTime() && reminder.isInPast()) {
                Timber.tag("Reminder").d("schedule: delete")
                deleteReminderInternal(reminder)
            }

            if (reminder.isOneTime() && !reminder.isInPast()) {
                Timber.tag("Reminder").d("schedule: add one-time")
                addReminder(reminder)
            }

            if (!reminder.isOneTime() && reminder.isInPast()) {
                Timber.tag("Reminder").d("schedule: update")
                updateReminder(reminder)
            }

            if (!reminder.isOneTime() && !reminder.isInPast()) {
                Timber.tag("Reminder").d("schedule: add")
                addReminder(reminder)
            }
        }
    }

    private fun updateReminderInternal(reminder: Reminder) {
        val params = UpdateReminderUseCase.Params(reminder)
        updateReminderUseCase.execute(params).blockingGet()
    }

    private fun deleteReminderInternal(reminder: Reminder) {
        val params = DeleteReminderUseCase.Params(reminder)
        deleteReminderUseCase.execute(params).blockingGet()
    }

    private fun showReminderNotification(reminder: Reminder) {
        notificationManager.sendNotification(
            title = reminder.title,
            text = reminder.title,
            id = reminder.id
        )
    }
}