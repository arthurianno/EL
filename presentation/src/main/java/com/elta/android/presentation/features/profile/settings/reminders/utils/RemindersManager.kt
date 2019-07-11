package com.elta.android.presentation.features.profile.settings.reminders.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.os.Build
import com.elta.android.common.utils.toMillis
import com.elta.android.domain.features.reminder.interactor.DeleteReminderUseCase
import com.elta.android.domain.features.reminder.interactor.GetRemindersUseCase
import com.elta.android.domain.features.reminder.interactor.UpdateReminderUseCase
import com.elta.android.domain.features.reminder.interactor.getNextReminder
import com.elta.android.domain.features.reminder.interactor.isInThePast
import com.elta.android.domain.features.reminder.interactor.isOneTime
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.date.DateChangedEvent
import com.elta.android.presentation.core.notification.NotificationSource
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus
import io.reactivex.Observable
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
    private val resources: ResourceProvider,
    private val context: Context,
    private val bus: RxBus
) {

    private val manager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        Observable.merge(
            bus.events<Events.BootCompleted>().map { Unit },
            bus.events<Events.PackageReplaced>().map { Unit },
            bus.events<DateChangedEvent>().map { Unit }
        )
            .doOnNext { scheduleReminders() }
            .retry()
            .subscribe()

        bus.events<Events.ReminderSpent>()
            .map(Events.ReminderSpent::reminder)
            .doOnNext(::showReminderNotification)
            .doOnNext(::updateReminder)
            .retry()
            .subscribe()
    }

    fun addReminder(reminder: Reminder) {
        val pi = reminder.getPendingIntent(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.date.toMillis(), pi)
        else
            manager.setExact(AlarmManager.RTC_WAKEUP, reminder.date.toMillis(), pi)
    }

    fun updateReminder(reminder: Reminder) {
        getNextReminder(reminder)?.let { newReminder ->
            addReminder(newReminder)
            updateReminderInternal(newReminder)
        } ?: deleteReminderInternal(reminder)
    }

    fun cancelReminder(reminderId: String) {
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
            if (!reminder.isInThePast()) addReminder(reminder)
            else if (!reminder.isOneTime()) updateReminder(reminder)
            else deleteReminderInternal(reminder)
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
            title = resources.getString(R.string.profile_reminders_notification_title),
            text = reminder.title,
            id = reminder.id
        )
    }
}