package com.elta.android.presentation.jobs

import android.app.AlarmManager
import android.content.Context
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.model.ScheduleType
import com.elta.android.presentation.features.profile.settings.reminders.utils.getCancelPendingIntent
import com.elta.android.presentation.features.profile.settings.reminders.utils.toPendingIntent
import com.elta.android.presentation.utils.dayOfMonth
import com.elta.android.presentation.utils.hourOfDay
import com.elta.android.presentation.utils.minute
import com.elta.android.presentation.utils.month
import com.elta.android.presentation.utils.toCalendar
import com.elta.android.presentation.utils.year
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@Suppress("MagicNumber")
class ReminderWorker @Inject constructor(
    private val context: Context
) {

    private val manager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun addReminder(reminder: Reminder) {
        Timber.tag("Reminder").d("add ${reminder.title}")
        val pi = reminder.toPendingIntent(context)
        manager.setExact(AlarmManager.RTC_WAKEUP, reminder.time.time, pi)
    }

    fun updateReminder(reminder: Reminder) {
        Timber.tag("Reminder").d("update ${reminder.title}")
        val newReminder = calculateDelay(reminder)
        addReminder(newReminder)
    }

    fun cancelReminder(reminderId: String) {
        Timber.tag("Reminder").d("cancel $reminderId")
        val pi = getCancelPendingIntent(context, reminderId)
        manager.cancel(pi)
    }

    @Suppress("LongMethod")
    private fun calculateDelay(reminder: Reminder): Reminder {
        val reminderCalendar = reminder.time.toCalendar()
        val currentCalendar = Calendar.getInstance()
        currentCalendar.timeInMillis = System.currentTimeMillis()
        val delayCalendar: Calendar

        if (reminderCalendar.before(currentCalendar)) {
            delayCalendar = Calendar.getInstance()
            when (reminder.scheduleType) {
                ScheduleType.NONE -> delayCalendar.timeInMillis = -1
                ScheduleType.DAY -> {
                    delayCalendar.set(Calendar.YEAR, currentCalendar.year)
                    delayCalendar.set(Calendar.MONTH, currentCalendar.month)
                    delayCalendar.set(Calendar.DATE, currentCalendar.dayOfMonth)
                    delayCalendar.set(Calendar.HOUR, reminderCalendar.hourOfDay)
                    delayCalendar.set(Calendar.MINUTE, reminderCalendar.minute)
                    delayCalendar.set(Calendar.SECOND, 0)
                    delayCalendar.add(Calendar.DATE, 1)
                }
                ScheduleType.WEEK -> {
                    delayCalendar.set(Calendar.YEAR, currentCalendar.year)
                    delayCalendar.set(Calendar.MONTH, currentCalendar.month)
                    delayCalendar.set(Calendar.DATE, currentCalendar.dayOfMonth)
                    delayCalendar.set(Calendar.HOUR, reminderCalendar.hourOfDay)
                    delayCalendar.set(Calendar.MINUTE, reminderCalendar.minute)
                    delayCalendar.set(Calendar.SECOND, 0)
                    delayCalendar.add(Calendar.DATE, 7)
                }
                ScheduleType.MONTH -> {
                    delayCalendar.set(Calendar.YEAR, currentCalendar.year)
                    delayCalendar.set(Calendar.MONTH, currentCalendar.month)
                    delayCalendar.set(Calendar.DATE, currentCalendar.dayOfMonth)
                    delayCalendar.set(Calendar.HOUR, reminderCalendar.hourOfDay)
                    delayCalendar.set(Calendar.MINUTE, reminderCalendar.minute)
                    delayCalendar.set(Calendar.SECOND, 0)
                    delayCalendar.add(Calendar.MONTH, 1)
                }
                ScheduleType.YEAR -> {
                    delayCalendar.set(Calendar.YEAR, currentCalendar.year)
                    delayCalendar.set(Calendar.MONTH, reminderCalendar.month)
                    delayCalendar.set(Calendar.DATE, reminderCalendar.dayOfMonth)
                    delayCalendar.set(Calendar.HOUR, reminderCalendar.hourOfDay)
                    delayCalendar.set(Calendar.MINUTE, reminderCalendar.minute)
                    delayCalendar.set(Calendar.SECOND, 0)
                    delayCalendar.add(Calendar.YEAR, 1)
                }
            }
        } else {
            delayCalendar = reminderCalendar
        }

        return reminder.apply { time = delayCalendar.time }
    }
}