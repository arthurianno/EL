package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.devices.model.GlucometerInfo

@Suppress("TooManyFunctions", "ComplexInterface")
interface DeviceInfoRepository {

    fun getDevices(): List<Pair<Glucometer, GlucometerInfo>>

    fun getDevice(address: String): Glucometer?

    fun deleteDevice(address: String)

    fun getLastDeviceInfo(address: String): GlucometerInfo?

    fun setPrimaryDevice(address: String)

    fun getPrimaryDevice(): Pair<Glucometer, GlucometerInfo>?

    fun updateGlucometerInfo(glucometerInfo: GlucometerInfo, lastSyncedEvent: GlucometerEvent?)

}