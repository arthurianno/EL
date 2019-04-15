package com.elta.android.data.features.reminder.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.reminder.datasource.RemindersDataSource
import com.elta.android.data.features.reminder.dto.ReminderDto
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.repository.RemindersRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ReminderDataRepository @Inject constructor(
    private val toDtoMapper: Mapper<Reminder, ReminderDto>,
    private val toDomainMapper: Mapper<ReminderDto, Reminder>,
    @Cache private val source: RemindersDataSource
) : RemindersRepository {

    override fun getReminders(): Observable<List<Reminder>> =
        source.getReminders()
            .map(toDomainMapper::mapFromObjects)

    override fun getReminderById(id: String): Single<Reminder> =
        source.getReminderById(id)
            .map(toDomainMapper::mapFromObject)

    override fun addReminder(reminder: Reminder): Completable =
        createSingleRemindersDto(reminder)
            .flatMapCompletable { source.addReminders(it) }

    override fun updateReminder(reminder: Reminder): Completable =
        createSingleRemindersDto(reminder)
            .flatMapCompletable { source.updateReminders(it) }

    override fun deleteReminder(reminder: Reminder): Completable =
        createSingleRemindersDto(reminder)
            .flatMapCompletable { source.deleteReminders(it) }

    private fun createSingleRemindersDto(reminder: Reminder) =
        Single.fromCallable { listOf(toDtoMapper.mapFromObject(reminder)) }
}