package com.elta.android.data.features.devices.cgm.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Decoder for a five-point response to the NMG history command. */
object NmgHistoryPacketParser {
    private const val COMMAND_READ_HISTORY = 0x01
    private const val RESPONSE_SIZE_BYTES = 20
    private const val FIRST_POINT_OFFSET = 5
    private const val POINT_SIZE_BYTES = 3
    private const val POINT_INTERVAL_SECONDS = 120L
    private const val POINT_COUNT = 5

    fun parse(value: ByteArray): List<NmgHistoryPoint>? {
        if (value.size != RESPONSE_SIZE_BYTES || value[0].toInt() and 0xFF != COMMAND_READ_HISTORY) {
            return null
        }

        val buffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN)
        val firstTimestampSeconds = buffer.getInt(1).toUInt().toLong()
        return List(POINT_COUNT) { index ->
            val offset = FIRST_POINT_OFFSET + index * POINT_SIZE_BYTES
            NmgHistoryPoint(
                uptimeSeconds = firstTimestampSeconds + index * POINT_INTERVAL_SECONDS,
                temperatureCode = value[offset].toInt() and 0xFF,
                glucoseSignalNanoAmp = buffer.getShort(offset + 1).toInt() and 0xFFFF
            )
        }
    }
}

data class NmgHistoryPoint(
    val uptimeSeconds: Long,
    val temperatureCode: Int,
    val glucoseSignalNanoAmp: Int
)
