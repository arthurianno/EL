package com.elta.android.data.features.diary.events.mapper.v2

import com.elta.android.data.core.network.GsonFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.domain.features.diary.events.model.State
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class InvalidGlucometerEventSerializationTest {

    @Test
    fun `invalid measurement keeps device instant and flag in server payload`() {
        val deviceTime = ZonedDateTime.of(2019, 2, 22, 4, 0, 0, 0, ZoneOffset.UTC)
        val measurement = EventV2(
            id = "measurement-id",
            additionTime = deviceTime,
            tagId = null,
            tag = null,
            note = null,
            modificationTime = null,
            name = null,
            kind = null,
            temperature = 24.5,
            duration = null,
            activityType = null,
            mealTag = null,
            glucoseInputType = GlucoseInputType.AUTO,
            insulinMedicament = null,
            medicament = null,
            tabletsNumber = null,
            value = 4.4,
            type = EventType.Glucose(GlucoseInputType.AUTO),
            state = State.CREATED,
            glucometerSerialNumber = "D2204001234",
            dishes = emptyList(),
            isTimeInvalid = true
        )

        val request = EventV2ToDtoMapper().mapFromObject(measurement)
        val payload = GsonFactory.create().toJsonTree(request).asJsonObject

        assertEquals(deviceTime.toInstant(), ZonedDateTime.parse(request.additionTime).toInstant())
        assertTrue(request.data.isTimeInvalid)
        assertEquals(request.additionTime, payload.get("additionalTime").asString)
        assertTrue(payload.getAsJsonObject("data").get("isTimeInvalid").asBoolean)
    }
}
