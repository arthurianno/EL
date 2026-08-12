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
        with(source) {
            val cachedId = id.hashCode().toLong()
            val cacheInstance = eventsCache.get()
            val existingLocal = cacheInstance.get(CommonConditions.ById(cachedId))
                ?: (cacheInstance as? DbEventsV2Cache)?.getBySecondaryId(id)

            val serverModTime = modificationTime?.let { if (it < 10000000000L) it * 1000 else it }
            val localModTime = existingLocal?.modificationTime?.let { if (it < 10000000000L) it * 1000 else it }
            
            val isRemoteModification = if (serverModTime != null && localModTime != null) {
                kotlin.math.abs(serverModTime - localModTime) > 5000
            } else {
                false
            }

            val isTimeInvalidValue = if (modificationTime != null || isRemoteModification) {
                data.isTimeInvalid
            } else {
                data.isTimeInvalid || (existingLocal?.isTimeInvalid == true)
            }

            EventV2CachedDto(
                id = cachedId,
                secondaryId = id,
                type = data.type.name,
                additionTime = additionTime.toAdditionMillis(),
                additionTimeString = additionTime,
                tagId = tagId,
                note = note,
                modificationTime = modificationTime,
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
