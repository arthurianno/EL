package com.elta.android.data.features.devices.datasource

import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DeviceDataSource {

    fun findDevices(): Observable<List<GlucometerDto>>

    fun getGlucometerInfo(address: String): Single<GlucometerInfoDto>

    fun getGlucometerEvents(address: String): Single<List<String>>

    fun connectDevice(address: String, pinCode: String): Completable

    fun updateFirmware(address: String, firmwareFile: FirmwareFile): Completable
}