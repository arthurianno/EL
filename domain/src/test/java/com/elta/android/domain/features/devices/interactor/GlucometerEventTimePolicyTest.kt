package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.GlucometerEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class GlucometerEventTimePolicyTest {

    @Test
    fun `clock mismatch marks every new measurement without changing device dates`() {
        val firstDate = ZonedDateTime.of(2025, 1, 1, 9, 0, 0, 0, ZoneOffset.UTC)
        val secondDate = firstDate.minusDays(1)
        val events = listOf(event("first", firstDate), event("second", secondDate))

        val result = events.markInvalidTimeForOutOfSyncClock(true)

        assertEquals(listOf(firstDate, secondDate), result.map { it.date })
        assertEquals(listOf(true, true), result.map { it.isTimeInvalid })
    }

    @Test
    fun `a valid clock does not clear an invalid status from the measurement`() {
        val deviceDate = ZonedDateTime.of(2025, 1, 1, 9, 0, 0, 0, ZoneOffset.UTC)
        val event = event("flagged", deviceDate).copy(isTimeInvalid = true)

        assertEquals(listOf(event), listOf(event).markInvalidTimeForOutOfSyncClock(false))
    }

    private fun event(id: String, date: ZonedDateTime) = GlucometerEvent(
        id = id,
        date = date,
        temperature = null,
        value = 5.5,
        glucometerSerialNumber = null,
        originalResponse = id
    )
}
