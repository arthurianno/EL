package com.elta.android.data.features.devices.repository

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.glucometer.builder.DefaultGlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.client.GlucometerClient
import com.elta.android.data.features.devices.glucometer.generator.GlucometerEventIdGenerator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class DeviceDataRepositoryBuildEventsTest {

    @Test
    fun `unparseable rd date is skipped while the next measurement is retained`() = runBlocking {
        val validResponse = "rd220224040000245044"
        val invalidResponse = "rd991332999999245044"
        val dateFromDevice = ZonedDateTime.of(2022, 2, 24, 4, 0, 0, 0, ZoneOffset.UTC)
        val builder = object : DefaultGlucometerEventBuilder(
            object : GlucometerEventIdGenerator {
                override fun generate(userId: String, glucometerId: String, dateToken: String) = dateToken
            }
        ) {
            override fun extractDate(token: String): ZonedDateTime =
                if (token == "991332999999") throw IllegalArgumentException("Invalid date")
                else dateFromDevice
        }
        val repository = DeviceDataRepository(
            mock(GlucometerClient::class.java),
            mockMapper(),
            mockMapper(),
            mockMapper(),
            builder
        )

        val events = repository.buildEvents(
            address = "device",
            email = "user@example.org",
            serial = null,
            measurements = listOf(invalidResponse, validResponse),
            glucometerName = null
        )

        assertEquals(1, events.size)
        assertEquals(validResponse, events.single().originalResponse)
        assertEquals(dateFromDevice, events.single().date)
        assertEquals(false, events.single().isTimeInvalid)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <Source, Target> mockMapper(): Mapper<Source, Target> =
        mock(Mapper::class.java) as Mapper<Source, Target>
}
