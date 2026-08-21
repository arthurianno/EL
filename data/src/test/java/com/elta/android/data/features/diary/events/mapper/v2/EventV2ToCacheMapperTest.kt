package com.elta.android.data.features.diary.events.mapper.v2

import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.cache.Condition
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.events.cache.dto.v2.EventV2CachedDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.GlucoseInputTypeDto
import com.elta.android.data.features.diary.events.dto.v2.EventDataV2Dto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import org.junit.Assert.assertEquals
import org.junit.Test
import javax.inject.Provider

class EventV2ToCacheMapperTest {

    @Test
    fun `mapFromObject preserves local invalid time until user edits measurement`() {
        val eventId = "invalid-time-event"
        val existingLocal = cachedEvent(
            id = eventId.hashCode().toLong(),
            secondaryId = eventId,
            additionTime = 1_800_000_000_000L,
            additionTimeString = "2027-01-15T10:00:00.000000Z",
            modificationTime = null,
            isTimeInvalid = true
        )
        val mapper = EventV2ToCacheMapper(Provider { FakeEventsCache(existingLocal) })
        val serverEvent = eventDto(
            id = eventId,
            additionTime = "2020-01-01T00:00:00.000000Z",
            modificationTime = 1_800_000_100_000L,
            isTimeInvalid = false
        )

        val result = mapper.mapFromObject(serverEvent)

        assertEquals(true, result.isTimeInvalid)
        assertEquals(existingLocal.additionTime, result.additionTime)
        assertEquals(existingLocal.additionTimeString, result.additionTimeString)
        assertEquals(existingLocal.modificationTime, result.modificationTime)
    }

    @Test
    fun `mapFromObject clears local invalid time for user edit`() {
        val eventId = "invalid-time-event"
        val existingLocal = cachedEvent(
            id = eventId.hashCode().toLong(),
            secondaryId = eventId,
            additionTime = 1_800_000_000_000L,
            additionTimeString = "2027-01-15T10:00:00.000000Z",
            modificationTime = null,
            isTimeInvalid = true
        )
        val mapper = EventV2ToCacheMapper(Provider { FakeEventsCache(existingLocal) })
        val editedEvent = eventDto(
            id = eventId,
            additionTime = "2020-01-01T00:00:00.000000Z",
            modificationTime = 1_800_000_100_000L,
            isTimeInvalid = false
        )

        val result = mapper.mapFromObject(editedEvent, preserveLocalInvalidTime = false)

        assertEquals(false, result.isTimeInvalid)
        assertEquals(editedEvent.modificationTime, result.modificationTime)
        assertEquals(editedEvent.additionTime, result.additionTimeString)
    }

    private fun eventDto(
        id: String,
        additionTime: String,
        modificationTime: Long?,
        isTimeInvalid: Boolean
    ): EventV2Dto =
        EventV2Dto(
            id = id,
            additionTime = additionTime,
            tagId = null,
            note = null,
            modificationTime = modificationTime,
            state = StateDto.CREATED,
            data = EventDataV2Dto(
                temperature = null,
                duration = null,
                value = 5.5,
                kind = null,
                name = null,
                activityType = null,
                mealTag = null,
                inputType = GlucoseInputTypeDto.AUTO,
                insulinMedicament = null,
                medicament = null,
                tabletsNumber = null,
                type = EventTypeDto.GLUCOSE,
                glucometerSerialNumber = "D1234567890",
                products = null,
                productsCount = null,
                isTimeInvalid = isTimeInvalid,
                isTemperatureInvalid = false
            )
        )

    private fun cachedEvent(
        id: Long,
        secondaryId: String,
        additionTime: Long,
        additionTimeString: String,
        modificationTime: Long?,
        isTimeInvalid: Boolean
    ): EventV2CachedDto =
        EventV2CachedDto(
            id = id,
            secondaryId = secondaryId,
            type = EventTypeDto.GLUCOSE.name,
            additionTime = additionTime,
            additionTimeString = additionTimeString,
            tagId = null,
            note = null,
            modificationTime = modificationTime,
            products = null,
            temperature = null,
            value = 5.5,
            name = null,
            kind = null,
            duration = null,
            activityType = null,
            mealTag = null,
            glucoseInputType = GlucoseInputTypeDto.AUTO,
            medicament = null,
            medicamentDto = null,
            tabletsNumber = null,
            state = StateDto.CREATED.name,
            glucometerSerialNumber = "D1234567890",
            isTimeInvalid = isTimeInvalid,
            isTemperatureInvalid = false
        )
}

private class FakeEventsCache(
    private val event: EventV2CachedDto
) : Cache<EventV2CachedDto> {

    override fun add(objects: List<EventV2CachedDto>) = Unit

    override fun update(objects: List<EventV2CachedDto>) = Unit

    override fun delete(condition: Condition) = Unit

    override fun get(condition: Condition): EventV2CachedDto? =
        when (condition) {
            is CommonConditions.ById -> event.takeIf { it.id == condition.id }
            else -> null
        }

    override fun getAll(condition: Condition): List<EventV2CachedDto> = emptyList()

    override fun contains(condition: Condition): Boolean = get(condition) != null

    override fun count(condition: Condition): Long = if (contains(condition)) 1 else 0
}
