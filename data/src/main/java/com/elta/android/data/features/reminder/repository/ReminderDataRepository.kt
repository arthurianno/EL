package com.elta.android.data.features.reminder.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.errors.ReminderAlreadyExistsError
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.reminder.datasource.RemindersDataSource
import com.elta.android.data.features.reminder.dto.ReminderDto
import com.elta.android.data.features.reminder.dto.ScheduleTypeDto
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.repository.RemindersRepository
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

class ReminderDataRepository @Inject constructor(
    private val toDtoMapper: Mapper<Reminder, ReminderDto>,
    private val toDomainMapper: Mapper<ReminderDto, Reminder>,
    @Cache private val source: RemindersDataSource
) : RemindersRepository {

    override fun getReminders(): Observable<List<Reminder>> =
        source.getReminders()
            .flatMap(::deleteUselessReminders)
            .map(toDomainMapper::mapFromObjects)

    override fun getReminderById(id: String): Single<Reminder> =
        source.getReminderById(id)
            .map(toDomainMapper::mapFromObject)

    override fun addReminder(reminder: Reminder): Single<String> {
        val allRemindersDto = getReminders().map(toDtoMapper::mapFromObjects)
        val currentReminderAsListDto = createSingleRemindersDto(reminder).toObservable()

        return Observable.zip(
            currentReminderAsListDto,
            allRemindersDto
        ) { reminderAsList, allReminders ->
            val remindersDateToTimeList =
                allReminders.map { it.time.toLocalDate() to it.time.toLocalTime() }
            val currentReminderDateToTime =
                reminderAsList.map { it.time.toLocalDate() to it.time.toLocalTime() }

            if (remindersDateToTimeList.containsAll(currentReminderDateToTime)) throw ReminderAlreadyExistsError()

            reminderAsList
        }
            .flatMapCompletable { source.addReminders(it) }
            .toSingleDefault(reminder.id)
    }

    override fun updateReminder(reminder: Reminder): Single<String> =
        createSingleRemindersDto(reminder)
            .flatMapCompletable { source.updateReminders(it) }
            .toSingleDefault(reminder.id)

    override fun deleteReminder(reminder: Reminder): Single<String> =
        createSingleRemindersDto(reminder)
            .flatMapCompletable { source.deleteReminders(it) }
            .toSingleDefault(reminder.id)

    private fun deleteUselessReminders(reminders: List<ReminderDto>): Observable<MutableList<ReminderDto>> {
        val filteredReminders = mutableListOf<ReminderDto>()
        val uselessReminders = mutableListOf<ReminderDto>()
        reminders.forEach {
            if (it.scheduleType == ScheduleTypeDto.NONE && it.time.isBefore(ZonedDateTime.now())) {
                uselessReminders.add(it)
            } else {
                filteredReminders.add(it)
            }
        }
        return source.deleteReminders(uselessReminders)
            .andThen(Observable.just(filteredReminders))
    }

    private fun createSingleRemindersDto(reminder: Reminder) =
        Single.fromCallable { listOf(toDtoMapper.mapFromObject(reminder)) }
}
