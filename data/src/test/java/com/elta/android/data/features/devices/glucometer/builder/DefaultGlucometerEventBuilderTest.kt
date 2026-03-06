package com.elta.android.data.features.devices.glucometer.builder

import com.elta.android.data.features.devices.glucometer.generator.GlucometerEventIdGenerator
import com.elta.android.domain.features.diary.events.model.MealTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class DefaultGlucometerEventBuilderTest {

    private val builder = TestableDefaultGlucometerEventBuilder(generator = FakeGenerator())

    @Test
    fun `buildFrom parses rd before meal event`() {
        val event = builder.buildFrom(
            userId = "user",
            glucometerId = "device",
            response = "rd220224040000245044",
            glucometerSerialNumber = "D2204001234",
            glucometerName = "SatelliteOnline0001"
        )

        assertEquals(24.5, event.temperature ?: -1.0, 0.0)
        assertEquals(4.4, event.value ?: -1.0, 0.0)
        assertEquals(MealTag.BEFOREMEAL, event.mealTag)
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
}

private class FakeGenerator : GlucometerEventIdGenerator {
    override fun generate(userId: String, glucometerId: String, dateToken: String): String =
        "$userId-$glucometerId-$dateToken"
}

private class TestableDefaultGlucometerEventBuilder(
    generator: GlucometerEventIdGenerator
) : DefaultGlucometerEventBuilder(generator) {
    override fun extractDate(token: String): ZonedDateTime =
        ZonedDateTime.of(2024, 2, 22, 4, 0, 0, 0, ZoneOffset.UTC)
}
