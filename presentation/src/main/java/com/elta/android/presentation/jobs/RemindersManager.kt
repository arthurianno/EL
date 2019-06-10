package com.elta.android.presentation.jobs

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
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
import com.elta.android.presentation.features.profile.settings.reminders.utils.getCancelPendingIntent
import com.elta.android.presentation.features.profile.settings.reminders.utils.getPendingIntent
import com.nullgr.core.rx.RxBus
import timber.log.Timber
import javax.inject.Inject

@Suppress("MagicNumber")
@SuppressLint("CheckResult")
class RemindersManager @Inject constructor(
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val getRemindersUseCase: GetRemindersUseCase,
    private val context: Context,
    private val bus: RxBus
) {

    private val manager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        bus.events<Events.BootCompleted>()
            .subscribe { bootComplete() }
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

    fun bootComplete() {
        val reminders = getRemindersUseCase.execute().blockingFirst()
        reminders.forEach { reminder ->
            if (reminder.isOneTime() && reminder.isInPast()) {
                Timber.tag("Reminder").d("boot: delete")
                deleteReminderInternal(reminder)
            }

            if (reminder.isOneTime() && !reminder.isInPast()) {
                Timber.tag("Reminder").d("boot: add one-time")
                addReminder(reminder)
            }

            if (!reminder.isOneTime() && reminder.isInPast()) {
                Timber.tag("Reminder").d("boot: update")
                updateReminder(reminder)
            }

            if (!reminder.isOneTime() && !reminder.isInPast()) {
                Timber.tag("Reminder").d("boot: add")
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
}