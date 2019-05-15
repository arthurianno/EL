package com.elta.android.data.features.devices.datasource

import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DeviceDataSource {

    fun findDevices(): Observable<List<GlucometerDto>>

    fun getGlucometerInfo(address: String): Single<GlucometerInfoDto>

    fun getLastGlucometerInfo(address: String): Single<GlucometerInfoDto>

    fun getGlucometerEvents(address: String): Single<List<String>>

    fun connectDevice(device: GlucometerDto, pinCode: String): Completable

    fun syncWithDevice(device: GlucometerDto?): Single<List<GlucometerEventDto>>

    fun updateFirmware(address: String, firmwareFile: FirmwareFile): Completable
}