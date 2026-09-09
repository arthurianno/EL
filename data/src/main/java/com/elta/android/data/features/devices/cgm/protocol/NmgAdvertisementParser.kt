package com.elta.android.data.features.devices.cgm.protocol

import android.bluetooth.le.ScanRecord
import android.util.SparseArray

/**
 * Parses both forms returned by Android:
 * - a complete 19-byte protocol payload;
 * - manufacturer data with the two-byte company id already stripped by the platform.
 */
object NmgAdvertisementParser {
    fun parse(record: ScanRecord?): NmgAdvertisement? {
        val manufacturerData = record?.manufacturerSpecificData ?: return null
        return manufacturerData.findFirstValid()
    }

    private fun SparseArray<ByteArray>.findFirstValid(): NmgAdvertisement? {
        for (index in 0 until size()) {
            val companyId = keyAt(index)
            val payload = valueAt(index) ?: continue
            parse(payload, companyId)?.let { return it }
        }
        return null
    }

    internal fun parse(payload: ByteArray, companyId: Int? = null): NmgAdvertisement? {
        val offset = when {
            payload.size >= NmgAdvertisement.ADVERTISEMENT_SIZE_BYTES &&
                payload.uint16Le(0) == NmgAdvertisement.MANUFACTURER_ID -> 2
            companyId == NmgAdvertisement.MANUFACTURER_ID &&
                payload.size >= NmgAdvertisement.DATA_WITHOUT_MANUFACTURER_ID_SIZE_BYTES -> 0
            else -> return null
        }

        if (payload.size < offset + NmgAdvertisement.DATA_WITHOUT_MANUFACTURER_ID_SIZE_BYTES) return null

        return NmgAdvertisement(
            sensorId = payload.copyOfRange(offset, offset + MAC_LENGTH).toMacAddress(),
            state = payload.uint16Le(offset + STATE_OFFSET),
            uptimeSeconds = payload.uint32Le(offset + TIMESTAMP_OFFSET),
            temperatureCode = payload.uint8(offset + TEMPERATURE_OFFSET),
            glucoseSignalNanoAmp = payload.uint16Le(offset + GLUCOSE_OFFSET),
            currentNanoAmp = payload.uint16Le(offset + CURRENT_OFFSET)
        )
    }

    private fun ByteArray.uint8(offset: Int): Int = getOrNull(offset)?.toInt()?.and(BYTE_MASK) ?: 0

    private fun ByteArray.uint16Le(offset: Int): Int =
        uint8(offset) or (uint8(offset + 1) shl BITS_IN_BYTE)

    private fun ByteArray.uint32Le(offset: Int): Long =
        uint8(offset).toLong() or
            (uint8(offset + 1).toLong() shl 8) or
            (uint8(offset + 2).toLong() shl 16) or
            (uint8(offset + 3).toLong() shl 24)

    private fun ByteArray.toMacAddress(): String = joinToString(separator = ":") { byte ->
        "%02X".format(byte.toInt() and BYTE_MASK)
    }

    private const val MAC_LENGTH = 6
    private const val STATE_OFFSET = 6
    private const val TIMESTAMP_OFFSET = 8
    private const val TEMPERATURE_OFFSET = 12
    private const val GLUCOSE_OFFSET = 13
    private const val CURRENT_OFFSET = 15
    private const val BYTE_MASK = 0xFF
    private const val BITS_IN_BYTE = 8
}
