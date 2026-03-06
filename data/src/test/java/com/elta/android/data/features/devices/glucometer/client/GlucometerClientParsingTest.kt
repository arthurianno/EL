package com.elta.android.data.features.devices.glucometer.client

import org.junit.Assert.assertEquals
import org.junit.Test

class GlucometerClientParsingTest {

    @Test
    fun `extractBatteryAndTemperature parses protocol 2_07 battery format`() {
        val parsed = "b3t223".extractBatteryAndTemperature()
        assertEquals(3, parsed.first)
        assertEquals(223, parsed.second)
    }

    @Test
    fun `extractBatteryAndTemperature parses legacy battery format`() {
        val parsed = "b3.t223".extractBatteryAndTemperature()
        assertEquals(3, parsed.first)
        assertEquals(223, parsed.second)
    }

    @Test
    fun `extractVersion parses hardware and software tokens`() {
        val version = "hw:rev.r08 sw:4.0.5".extractVersion()
        assertEquals("rev.r08", version.hardware)
        assertEquals("4.0.5", version.software)
    }

    @Test
    fun `extractSerial parses serial payload`() {
        val serial = "ser.D2204001234".extractSerial()
        assertEquals("D2204001234", serial)
    }
}
