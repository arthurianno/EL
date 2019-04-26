package com.elta.android.data.features.devices.repository

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.datasource.DeviceDataSource
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class DeviceDataRepository @Inject constructor(
    private val glucometerToDtoMapper: Mapper<Glucometer, GlucometerDto>,
    private val glucometerToDomainMapper: Mapper<GlucometerDto, Glucometer>,
    private val glucometerInfoToDomainMapper: Mapper<GlucometerInfoDto, GlucometerInfo>,
    private val source: DeviceDataSource
) : DeviceRepository {

    override fun findDevices(): Observable<List<Glucometer>> =
        source.findDevices().map(glucometerToDomainMapper::mapFromObjects)

    override fun getDeviceInfo(address: String): Single<GlucometerInfo> =
        source.getGlucometerInfo(address).map(glucometerInfoToDomainMapper::mapFromObject)

    override fun getDeviceEvents(address: String): Single<List<String>> =
        source.getGlucometerEvents(address)

    override fun connectDevice(device: Glucometer, pinCode: String): Completable =
        source.connectDevice(glucometerToDtoMapper.mapFromObject(device), pinCode)

    override fun updateFirmware(address: String, firmwareFile: FirmwareFile): Completable =
        source.updateFirmware(address, firmwareFile)
}