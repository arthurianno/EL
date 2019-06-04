package com.elta.android.presentation.jobs

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elta.android.common.utils.toMillis
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
import com.nullgr.core.rx.RxBus
import io.reactivex.Single
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
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
                        .map { updatedReminder.time.toMillis() - ZonedDateTime.now().toMillis() }
                        .map { delay ->
                            if (delay > 0) startWithDelay(id = reminder.id, delay = delay)
                            else cancelByName(reminder.id)
                        }
                }
        }

    @Suppress("LongMethod")
    private fun calculateDelay(reminder: Reminder): Reminder {
        val reminderDate = reminder.time
        val currentDate = ZonedDateTime.now()

        val nextDate: ZonedDateTime = if (reminderDate.isBefore(currentDate)) {
            when (reminder.scheduleType) {
                ScheduleType.NONE -> ZonedDateTime.of(0, 0, 0, 0, 0, 0, 0, ZoneId.systemDefault())
                ScheduleType.DAY -> {
                    ZonedDateTime.from(currentDate)
                        .withHour(reminderDate.hour)
                        .withMinute(reminderDate.minute)
                        .withSecond(0)
                        .plusDays(1)
                }
                ScheduleType.WEEK -> {
                    ZonedDateTime.from(currentDate)
                        .withHour(reminderDate.hour)
                        .withMinute(reminderDate.minute)
                        .withSecond(0)
                        .plusDays(7)
                }
                ScheduleType.MONTH -> {
                    ZonedDateTime.from(currentDate)
                        .withHour(reminderDate.hour)
                        .withMinute(reminderDate.minute)
                        .withSecond(0)
                        .plusMonths(1)
                }
                ScheduleType.YEAR -> {
                    ZonedDateTime.from(currentDate)
                        .withMonth(reminderDate.monthValue)
                        .withDayOfMonth(reminderDate.dayOfMonth)
                        .withHour(reminderDate.hour)
                        .withMinute(reminderDate.minute)
                        .withSecond(0)
                        .plusYears(1)
                }
            }
        } else reminderDate

        return reminder.apply { time = nextDate }
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