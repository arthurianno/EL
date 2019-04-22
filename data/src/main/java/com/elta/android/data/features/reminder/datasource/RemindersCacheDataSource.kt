package com.elta.android.data.features.reminder.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.reminder.cache.dto.ReminderCacheDto
import com.elta.android.data.features.reminder.dto.ReminderDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class RemindersCacheDataSource @Inject constructor(
    private val toCacheMapper: Mapper<ReminderDto, ReminderCacheDto>,
    private val fromCacheMapper: Mapper<ReminderCacheDto, ReminderDto>,
    private val cache: Cache<ReminderCacheDto>
) : RemindersDataSource {

    override fun getReminders(): Observable<List<ReminderDto>> =
        Observable.fromCallable {
            cache.getAll(CommonConditions.All)
        }.map(fromCacheMapper::mapFromObjects)

    override fun getReminderById(id: String): Single<ReminderDto> =
        Single.fromCallable {
            cache.get(CommonConditions.ById(id.hashCode().toLong()))
        }.map(fromCacheMapper::mapFromObject)

    override fun addReminders(reminders: List<ReminderDto>): Completable =
        Completable.fromCallable {
            cache.add(toCacheMapper.mapFromObjects(reminders))
        }

    override fun updateReminders(reminders: List<ReminderDto>): Completable =
        Completable.fromCallable {
            cache.update(toCacheMapper.mapFromObjects(reminders))
        }

    override fun deleteReminders(reminders: List<ReminderDto>): Completable =
        Completable.fromCallable {
            cache.delete(CommonConditions.ByIds(reminders.map { it.id.hashCode().toLong() }))
        }
}