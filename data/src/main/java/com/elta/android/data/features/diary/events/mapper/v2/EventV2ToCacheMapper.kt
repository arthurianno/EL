package com.elta.android.data.features.diary.events.mapper.v2

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.events.cache.dto.v2.DbEventsV2Cache
import com.elta.android.data.features.diary.events.cache.dto.v2.EventV2CachedDto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import com.elta.android.data.features.diary.events.extensions.toAdditionMillis
import com.elta.android.data.features.diary.events.mapper.toCache
import javax.inject.Inject
import javax.inject.Provider

class EventV2ToCacheMapper @Inject constructor(
    private val eventsCache: Provider<Cache<EventV2CachedDto>>
) : Mapper<EventV2Dto, EventV2CachedDto> {

    override fun mapFromObject(source: EventV2Dto): EventV2CachedDto =
        mapFromObject(source, preserveLocalInvalidTime = true)

    fun mapFromObject(source: EventV2Dto, preserveLocalInvalidTime: Boolean): EventV2CachedDto =
        with(source) {
            val cachedId = id.hashCode().toLong()
            val cacheInstance = eventsCache.get()
            val existingLocal = cacheInstance.get(CommonConditions.ById(cachedId))
                ?: (cacheInstance as? DbEventsV2Cache)?.getBySecondaryId(id)

            val incomingModTime = modificationTime?.let { if (it < 10000000000L) it * 1000 else it }
            val localModTime = existingLocal?.modificationTime?.let { if (it < 10000000000L) it * 1000 else it }
            val keepLocalInvalidTime =
                preserveLocalInvalidTime &&
                    existingLocal?.isTimeInvalid == true &&
                    existingLocal.modificationTime == null

            val isTimeInvalidValue = when {
                existingLocal == null -> data.isTimeInvalid
                keepLocalInvalidTime -> true
                incomingModTime == null -> data.isTimeInvalid || existingLocal.isTimeInvalid
                localModTime == null -> data.isTimeInvalid
                incomingModTime > localModTime + MODIFICATION_TIME_TOLERANCE_MILLIS -> data.isTimeInvalid
                else -> data.isTimeInvalid && existingLocal.isTimeInvalid
            }
            val additionTimeValue = if (keepLocalInvalidTime) {
                existingLocal.additionTime
            } else {
                additionTime.toAdditionMillis()
            }
            val additionTimeStringValue = if (keepLocalInvalidTime) {
                existingLocal.additionTimeString
            } else {
                additionTime
            }
            val modificationTimeValue = if (keepLocalInvalidTime) {
                existingLocal.modificationTime
            } else {
                modificationTime
            }

            EventV2CachedDto(
                id = cachedId,
                secondaryId = id,
                type = data.type.name,
                additionTime = additionTimeValue,
                additionTimeString = additionTimeStringValue,
                tagId = tagId,
                note = note,
                modificationTime = modificationTimeValue,
                value = data.value,
                kind = data.kind,
                name = data.name,
                duration = data.duration,
                activityType = data.activityType?.name,
                mealTag = data.mealTag?.name,
                medicament = data.insulinMedicament,
                medicamentDto = data.medicament,
                tabletsNumber = data.tabletsNumber,
                temperature = data.temperature,
                state = state.name,
                glucometerSerialNumber = data.glucometerSerialNumber,
                products = data.products.toCache(),
                glucoseInputType = data.inputType,
                isTimeInvalid = isTimeInvalidValue,
                isTemperatureInvalid = data.isTemperatureInvalid
            )
        }
}

private const val MODIFICATION_TIME_TOLERANCE_MILLIS = 5000L
