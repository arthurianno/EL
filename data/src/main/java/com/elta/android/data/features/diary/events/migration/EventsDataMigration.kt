package com.elta.android.data.features.diary.events.migration

import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.events.cache.dto.v1.EventCachedDto
import com.elta.android.data.features.diary.events.cache.dto.v2.EventV2CachedDto
import com.elta.android.data.features.diary.events.dto.v2.MedicamentDto
import com.elta.android.domain.features.diary.events.migration.EventsMigration
import io.reactivex.Completable
import javax.inject.Inject

/**
 *
 * A file for migrating local data to a user database.
 * Was created due to the replacement of insulin types.
 * Do not delete until the minimum version of the application is 2.2.0.
 *
 * */
class EventsDataMigration @Inject constructor(
    private val cache: Cache<EventCachedDto>,
    private val cacheV2: Cache<EventV2CachedDto>
) : EventsMigration {

    override fun migrationEventsToEventsV2(): Completable {
        return Completable.fromCallable {
            val events = cache.getAll(CommonConditions.All)

            if (events.isNotEmpty()) {
                val eventsV2 = events.map { it.toV2() }
                cache.delete(CommonConditions.All)
                cacheV2.add(eventsV2)
            }
        }
    }

}

private fun EventCachedDto.toV2(): EventV2CachedDto {
    return EventV2CachedDto(
        id = id,
        secondaryId = secondaryId,
        type = type,
        additionTime = additionTime,
        tagId = tagId,
        additionTimeString = additionTimeString,
        note = note,
        modificationTime = modificationTime,
        products = products,
        temperature = temperature,
        value = value,
        name = name,
        kind = kind,
        duration = duration,
        activityType = activityType,
        mealTag = mealTag,
        state = state,
        glucometerSerialNumber = glucometerSerialNumber,
        medicament = toMedicament(medicament, insulinType)
    )
}

fun toMedicament(medicament: String?, insulinType: String?): MedicamentDto? {
    if (medicament == null || insulinType == null) return null

    return MedicamentDto(
        id = medicament.hashCode(),
        name = medicament,
        insulinType = MedicamentDto.MedicamentInsulinTypeDto(
            code = insulinType.convertCode().name,
            id = insulinType.hashCode(),
            name = insulinType.getName()
        ),
        deleted = false
    )
}

private fun String.convertCode(): InsulinType {
    return when (this) {
        InsulinType.ULTRAFAST.name,
        InsulinType.ULTRASHORT.name,
        InsulinType.SHORT.name -> InsulinType.ULTRASHORT

        InsulinType.INTERMEDIATE.name,
        InsulinType.LONG.name,
        InsulinType.ULTRALONG.name -> InsulinType.PROLONG

        InsulinType.MIXED.name -> InsulinType.MIXED

        else -> InsulinType.MIXED
    }
}

private fun String.getName(): String {
    return when (this) {
        InsulinType.ULTRAFAST.name,
        InsulinType.ULTRASHORT.name,
        InsulinType.SHORT.name -> SHORT_ULTRASHORT

        InsulinType.INTERMEDIATE.name,
        InsulinType.LONG.name,
        InsulinType.ULTRALONG.name -> PROLONG

        InsulinType.MIXED.name -> MIXED

        else -> MIXED
    }
}

private enum class InsulinType {
    ULTRAFAST,
    ULTRASHORT,
    SHORT,
    PROLONG,
    INTERMEDIATE,
    LONG,
    ULTRALONG,
    MIXED
}

private const val SHORT_ULTRASHORT = "Короткий"
private const val PROLONG = "Продленный"
private const val MIXED = "Смешанный"