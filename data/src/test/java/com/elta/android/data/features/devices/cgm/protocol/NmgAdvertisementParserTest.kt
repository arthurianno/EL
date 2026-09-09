package com.elta.android.data.features.devices.cgm.protocol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NmgAdvertisementParserTest {
    @Test
    fun `parses full protocol packet in little endian`() {
        val packet = byteArrayOf(
            0x55, 0xAA.toByte(),
            0x90.toByte(), 0x7B, 0xC6.toByte(), 0x2C, 0x62, 0xB6.toByte(),
            0x05, 0x00,
            0x78, 0x56, 0x34, 0x12,
            0x64,
            0x30, 0x75,
            0x20, 0x4E
        )

        val result = NmgAdvertisementParser.parse(packet)

        requireNotNull(result)
        assertEquals("90:7B:C6:2C:62:B6", result.sensorId)
        assertEquals(5, result.state)
        assertEquals(0x12345678L, result.uptimeSeconds)
        assertEquals(100, result.temperatureCode)
        assertEquals(30_000, result.glucoseSignalNanoAmp)
        assertEquals(20_000, result.currentNanoAmp)
    }

    @Test
    fun `parses manufacturer payload after Android strips company id`() {
        val payload = byteArrayOf(
            0x90.toByte(), 0x7B, 0xC6.toByte(), 0x2C, 0x62, 0xB6.toByte(),
            0x00, 0x00,
            0x78, 0x00, 0x00, 0x00,
            0x64,
            0x30, 0x75,
            0x20, 0x4E
        )

        val result = NmgAdvertisementParser.parse(payload, NmgAdvertisement.MANUFACTURER_ID)

        requireNotNull(result)
        assertEquals("90:7B:C6:2C:62:B6", result.sensorId)
        assertEquals(120, result.uptimeSeconds)
    }

    @Test
    fun `rejects packet with unsupported manufacturer id`() {
        val packet = ByteArray(NmgAdvertisement.ADVERTISEMENT_SIZE_BYTES)
        assertNull(NmgAdvertisementParser.parse(packet))
    }
}
