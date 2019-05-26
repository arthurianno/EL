package com.elta.android.presentation.jobs

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elta.android.domain.features.reminder.interactor.DeleteReminderUseCase
import com.elta.android.domain.features.reminder.interactor.GetReminderByIdUseCase
import com.elta.android.domain.features.reminder.interactor.UpdateReminderUseCase
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.model.ScheduleType
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.notification.NotificationSource
import com.elta.android.presentation.jobs.factory.JobFactory
import com.elta.android.presentation.utils.dayOfMonth
import com.elta.android.presentation.utils.hourOfDay
import com.elta.android.presentation.utils.minute
import com.elta.android.presentation.utils.month
import com.elta.android.presentation.utils.toCalendar
import com.elta.android.presentation.utils.year
import com.nullgr.core.rx.RxBus
import io.reactivex.Single
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Suppress("MagicNumber")
class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val getReminderByIdUseCase: GetReminderByIdUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val bus: RxBus,
    private val notificationSource: NotificationSource
) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        val isShowNotification = inputData.getBoolean(IS_SHOW_NOTIFICATION, false)
        val isActionDelete = inputData.getBoolean(IS_ACTION_DELETE, false)
        val reminderId: String = requireNotNull(inputData.getString(ID_KEY))

        if (isActionDelete) {
            cancelByName(reminderId)
            return Single.just(Result.success())
        }

        return Single.just(createGetReminderUseCaseParams(reminderId))
            .flatMap {
                getReminderByIdUseCase.execute(it)
                    .doOnSuccess { reminder -> sendNotificationIfNeed(isShowNotification, reminder) }
                    .flatMap { reminder -> deleteOrUpdateReminder(reminder, isShowNotification) }
            }
            .map { Result.success() }
            .onErrorReturn { Result.success() }
    }

    private fun cancelByName(reminderId: String) {
        WorkManager.getInstance().cancelUniqueWork(reminderId)
    }

    private fun createGetReminderUseCaseParams(id: String) =
        GetReminderByIdUseCase.Params(id)

    private fun createDeleteReminderUseCaseParams(reminder: Reminder) =
        DeleteReminderUseCase.Params(reminder)

    private fun sendNotificationIfNeed(isShowNotification: Boolean, reminder: Reminder) {
        if (isShowNotification) {
            notificationSource.sendNotification(
                title = applicationContext.getString(R.string.profile_reminders_notification_title),
                text = reminder.title,
                id = reminder.id)
        }
    }

    private fun deleteOrUpdateReminder(reminder: Reminder, isShowNotification: Boolean) =
        if (reminder.scheduleType == ScheduleType.NONE && isShowNotification) {
            deleteReminderUseCase.execute(createDeleteReminderUseCaseParams(reminder))
                .doOnSuccess { bus.event(Events.ReminderDeleted) }
                .map { Unit }
        } else {
            Single.just(reminder)
                .map(::calculateDelay)
                .flatMap { updatedReminder ->
                    updateReminderUseCase.execute(UpdateReminderUseCase.Params(updatedReminder))
                        .map { updatedReminder.time.time - System.currentTimeMillis() }
                        .map { delay ->
                            if (delay > 0) startWithDelay(id = reminder.id, delay = delay)
                            else cancelByName(reminder.id)
                        }
                }
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
                    delayCalendar.set(Calendar.DAY_OF_MONTH, currentCalendar.dayOfMonth)
                    delayCalendar.set(Calendar.HOUR_OF_DAY, reminderCalendar.hourOfDay)
                    delayCalendar.set(Calendar.MINUTE, reminderCalendar.minute)
                    delayCalendar.set(Calendar.SECOND, 0)
                    delayCalendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                ScheduleType.WEEK -> {
                    delayCalendar.set(Calendar.YEAR, currentCalendar.year)
                    delayCalendar.set(Calendar.MONTH, currentCalendar.month)
                    delayCalendar.set(Calendar.DAY_OF_MONTH, currentCalendar.dayOfMonth)
                    delayCalendar.set(Calendar.HOUR_OF_DAY, reminderCalendar.hourOfDay)
                    delayCalendar.set(Calendar.MINUTE, reminderCalendar.minute)
                    delayCalendar.set(Calendar.SECOND, 0)
                    delayCalendar.add(Calendar.DAY_OF_MONTH, 7)
                }
                ScheduleType.MONTH -> {
                    delayCalendar.set(Calendar.YEAR, currentCalendar.year)
                    delayCalendar.set(Calendar.MONTH, currentCalendar.month)
                    delayCalendar.set(Calendar.DAY_OF_MONTH, currentCalendar.dayOfMonth)
                    delayCalendar.set(Calendar.HOUR_OF_DAY, reminderCalendar.hourOfDay)
                    delayCalendar.set(Calendar.MINUTE, reminderCalendar.minute)
                    delayCalendar.set(Calendar.SECOND, 0)
                    delayCalendar.add(Calendar.MONTH, 1)
                }
                ScheduleType.YEAR -> {
                    delayCalendar.set(Calendar.YEAR, currentCalendar.year)
                    delayCalendar.set(Calendar.MONTH, reminderCalendar.month)
                    delayCalendar.set(Calendar.DAY_OF_MONTH, reminderCalendar.dayOfMonth)
                    delayCalendar.set(Calendar.HOUR_OF_DAY, reminderCalendar.hourOfDay)
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

    private fun startWithDelay(id: String, delay: Long) {
        val data = Data.Builder()
            .putString(ID_KEY, id)
            .putBoolean(IS_SHOW_NOTIFICATION, true)
            .build()

        val request = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(REMINDER_TAG)
            .build()

        WorkManager.getInstance()
            .enqueueUniqueWork(id, ExistingWorkPolicy.REPLACE, request)
    }

    class Factory @Inject constructor(
        private val getReminderByIdUseCase: GetReminderByIdUseCase,
        private val deleteReminderUseCase: DeleteReminderUseCase,
        private val updateReminderUseCase: UpdateReminderUseCase,
        private val bus: RxBus,
        private val notificationSource: NotificationSource
    ) : JobFactory<ReminderWorker> {
        override fun create(appContext: Context, params: WorkerParameters) =
            ReminderWorker(
                appContext,
                params,
                getReminderByIdUseCase,
                deleteReminderUseCase,
                updateReminderUseCase,
                bus,
                notificationSource
            )
    }

    companion object {
        private const val ID_KEY = "id_key"
        private const val IS_SHOW_NOTIFICATION = "is_show_notification"
        private const val REMINDER_TAG = "reminder_tag"
        private const val IS_ACTION_DELETE = "is_action_delete"

        fun startReminder(id: String, isActionDelete: Boolean = false) {

            val data = Data.Builder()
                .putString(ID_KEY, id)
                .putBoolean(IS_ACTION_DELETE, isActionDelete)
                .build()

            val request = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
                .setInputData(data)
                .addTag(REMINDER_TAG)
                .build()

            WorkManager.getInstance()
                .enqueueUniqueWork(id, ExistingWorkPolicy.REPLACE, request)
        }

        fun cancelReminder(id: String) {
            WorkManager.getInstance().cancelUniqueWork(id)
        }
    }
}