package com.elta.android.presentation.jobs

import android.app.AlarmManager
import android.content.Context
import com.elta.android.common.utils.toMillis
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.model.ScheduleType
import com.elta.android.presentation.features.profile.settings.reminders.utils.getCancelPendingIntent
import com.elta.android.presentation.features.profile.settings.reminders.utils.toPendingIntent
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import javax.inject.Inject

@Suppress("MagicNumber")
class ReminderWorker @Inject constructor(
    private val context: Context
) {

    private val manager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun addReminder(reminder: Reminder) {
        Timber.tag("Reminder").d("add ${reminder.title}")
        val pi = reminder.toPendingIntent(context)
        manager.setExact(AlarmManager.RTC_WAKEUP, reminder.time.toMillis(), pi)
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
        val reminderDate = reminder.time
        val currentDate = ZonedDateTime.now()

        val nextDate: ZonedDateTime = if (reminderDate.isBefore(currentDate)) {
            when (reminder.scheduleType) {
                ScheduleType.NONE -> ZonedDateTime.of(0, 0, 0, 0, 0, 0, 0, ZoneId.systemDefault())
                ScheduleType.DAY ->
                    ZonedDateTime.from(currentDate)
                        .withHour(reminderDate.hour)
                        .withMinute(reminderDate.minute)
                        .withSecond(0)
                        .plusDays(1)
                ScheduleType.WEEK ->
                    ZonedDateTime.from(currentDate)
                        .withHour(reminderDate.hour)
                        .withMinute(reminderDate.minute)
                        .withSecond(0)
                        .plusDays(7)
                ScheduleType.MONTH ->
                    ZonedDateTime.from(currentDate)
                        .withHour(reminderDate.hour)
                        .withMinute(reminderDate.minute)
                        .withSecond(0)
                        .plusMonths(1)
                ScheduleType.YEAR ->
                    ZonedDateTime.from(currentDate)
                        .withMonth(reminderDate.monthValue)
                        .withDayOfMonth(reminderDate.dayOfMonth)
                        .withHour(reminderDate.hour)
                        .withMinute(reminderDate.minute)
                        .withSecond(0)
                        .plusYears(1)
            }
        } else reminderDate

        return reminder.apply { time = nextDate }
    }
}