package com.elta.android.data.features.devices.glucometer.builder

import com.elta.android.data.features.devices.glucometer.generator.GlucometerEventIdGenerator
import com.elta.android.domain.features.diary.events.model.MealTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class DefaultGlucometerEventBuilderTest {

    private val builder = TestableDefaultGlucometerEventBuilder(generator = FakeGenerator())

    @Test
    fun `buildFrom does not preselect meal tag for rd event from non voice glucometer`() {
        val event = builder.buildFrom(
            userId = "user",
            glucometerId = "device",
            response = "rd220224040000245044",
            glucometerSerialNumber = "D2204001234",
            glucometerName = "SatelliteOnline0001"
        )

        assertEquals(24.5, event.temperature ?: -1.0, 0.0)
        assertEquals(4.4, event.value ?: -1.0, 0.0)
        assertNull(event.mealTag)
    }

    @Test
    fun `buildFrom parses rd after meal event`() {
        val event = builder.buildFrom(
            userId = "user",
            glucometerId = "device",
            response = "rd220224050000745044",
            glucometerSerialNumber = "D2204001234",
            glucometerName = "SatelliteVoice0001"
        )

        assertEquals(24.5, event.temperature ?: -1.0, 0.0)
        assertEquals(4.4, event.value ?: -1.0, 0.0)
        assertEquals(MealTag.AFTERMEAL, event.mealTag)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `buildFrom fails for empty rd event`() {
        builder.buildFrom(
            userId = "user",
            glucometerId = "device",
            response = "rd000000000000000000",
            glucometerSerialNumber = "D2204001234",
            glucometerName = "SatelliteOnline0001"
        )
    }

    @Test
    fun `buildFrom parses mem event with status flags`() {
        val event = builder.buildFrom(
            userId = "user",
            glucometerId = "device",
            response = "mem.690B559E00040064",
            glucometerSerialNumber = "D2204001234",
            glucometerName = "SatelliteVoice0001"
        )

        assertNull(event.temperature)
        assertEquals(10.0, event.value ?: -1.0, 0.0)
        assertEquals(MealTag.AFTERMEAL, event.mealTag)
    }

    @Test
    fun `buildFrom parses mem event without after meal status flag as before meal`() {
        val event = builder.buildFrom(
            userId = "user",
            glucometerId = "device",
            response = "mem.690B559E00000064",
            glucometerSerialNumber = "D2204001234",
            glucometerName = "SatelliteVoice0001"
        )

        assertNull(event.temperature)
        assertEquals(10.0, event.value ?: -1.0, 0.0)
        assertEquals(MealTag.BEFOREMEAL, event.mealTag)
    }

    @Test
    fun `buildFrom does not preselect meal tag for mem event from non voice glucometer`() {
        val event = builder.buildFrom(
            userId = "user",
            glucometerId = "device",
            response = "mem.690B559E00000064",
            glucometerSerialNumber = "D2204001234",
            glucometerName = "SatelliteExpress0001"
        )

        assertNull(event.temperature)
        assertEquals(10.0, event.value ?: -1.0, 0.0)
        assertNull(event.mealTag)
    }

    @Test
    fun `buildFrom keeps device date and does not mark mem invalid time status`() {
        val event = builder.buildFrom(
            userId = "user",
            glucometerId = "device",
            response = "mem.690B559E00010064",
            glucometerSerialNumber = "D2204001234",
            glucometerName = "SatelliteVoice0001"
        )

        assertEquals(false, event.isTimeInvalid)
        assertEquals(
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(0x690B559EL), ZoneOffset.UTC),
            event.date
        )
    }

    @Test
    fun `buildFrom parses mem event with invalid temperature status flag`() {
        val event = builder.buildFrom(
            userId = "user",
            glucometerId = "device",
            response = "mem.690B559E00020064",
            glucometerSerialNumber = "D2204001234",
            glucometerName = "SatelliteVoice0001"
        )

        assertEquals(true, event.isTemperatureInvalid)
    }

    @Test
    fun `buildFrom keeps future device date without marking it invalid`() {
        val futureBuilder = DefaultGlucometerEventBuilder(generator = FakeGenerator())
        val futureUnixHex = (ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() + 3600).toString(16).uppercase()
        val event = futureBuilder.buildFrom(
            userId = "user",
            glucometerId = "device",
            response = "mem.${futureUnixHex}00000064",
            glucometerSerialNumber = "D2204001234",
            glucometerName = "SatelliteVoice0001"
        )

        assertEquals(false, event.isTimeInvalid)
        assertEquals(
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(futureUnixHex.toLong(16)), ZoneOffset.UTC),
            event.date
        )
    }

    @Test
    fun `buildFrom keeps old rd date without marking it invalid`() {
        val deviceDate = ZonedDateTime.of(2019, 2, 22, 4, 0, 0, 0, ZoneOffset.UTC)
        val oldDateBuilder = TestableDefaultGlucometerEventBuilder(FakeGenerator(), deviceDate)

        val event = oldDateBuilder.buildFrom(
            userId = "user",
            glucometerId = "device",
            response = "rd190222040000245044",
            glucometerSerialNumber = "D2204001234",
            glucometerName = "SatelliteOnline0001"
        )

        assertEquals(deviceDate, event.date)
        assertEquals(false, event.isTimeInvalid)
    }
}

private class FakeGenerator : GlucometerEventIdGenerator {
    override fun generate(userId: String, glucometerId: String, dateToken: String): String =
        "$userId-$glucometerId-$dateToken"
}

private class TestableDefaultGlucometerEventBuilder(
    generator: GlucometerEventIdGenerator,
    private val parsedDate: ZonedDateTime = ZonedDateTime.of(2024, 2, 22, 4, 0, 0, 0, ZoneOffset.UTC)
) : DefaultGlucometerEventBuilder(generator) {
    override fun extractDate(token: String): ZonedDateTime =
        parsedDate
}
