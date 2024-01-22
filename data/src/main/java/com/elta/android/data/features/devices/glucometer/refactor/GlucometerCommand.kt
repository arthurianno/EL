package com.elta.android.data.features.devices.glucometer.refactor

import android.bluetooth.BluetoothDevice
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.dto.VersionDto
import org.threeten.bp.ZonedDateTime

interface GlucometerCommand {

    suspend fun connectToGlucometer(device: BluetoothDevice)

    suspend fun disconnectGlucometer()

    suspend fun checkPin(pin: String): Boolean
    suspend fun toDfuMode(): String

    suspend fun getDate(): ZonedDateTime?

    suspend fun getVersion(): VersionDto

    suspend fun getBatteryAndTemperature(): Pair<Int, Int>

    suspend fun turnOnFindMode(): String

    suspend fun updateTime(date: ZonedDateTime): String

    suspend fun readEvent(index: Int): String

    suspend fun getSerialNumber(): String

}