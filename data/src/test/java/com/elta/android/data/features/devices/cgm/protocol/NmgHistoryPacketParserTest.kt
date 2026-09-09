package com.elta.android.data.features.devices.cgm.protocol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NmgHistoryPacketParserTest {
    @Test
    fun `decodes five little endian history points`() {
        val packet = byteArrayOf(
            0x01, 0x10, 0x0E, 0x00, 0x00,
            0x64, 0x34, 0x12,
            0x65, 0x78, 0x56,
            0x66, 0xBC.toByte(), 0x9A.toByte(),
            0x67, 0xF0.toByte(), 0xDE.toByte(),
            0x68, 0x01, 0x00
        )

        val points = requireNotNull(NmgHistoryPacketParser.parse(packet))

        assertEquals(5, points.size)
        assertEquals(3_600L, points.first().uptimeSeconds)
        assertEquals(0x64, points.first().temperatureCode)
        assertEquals(0x1234, points.first().glucoseSignalNanoAmp)
        assertEquals(4_080L, points.last().uptimeSeconds)
        assertEquals(1, points.last().glucoseSignalNanoAmp)
    }

    @Test
    fun `rejects malformed packet`() {
        assertNull(NmgHistoryPacketParser.parse(byteArrayOf(0x01)))
    }
}
