package com.elta.android.data.features.devices.repository

import android.bluetooth.le.ScanResult
import com.elta.android.common.mapper.Mapper
import com.elta.android.common.repository.BaseRepository
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.manager.GlucometersInfoManager
import com.elta.android.data.features.devices.glucometer.service.GlucometersService
import com.elta.android.data.features.devices.glucometer.service.firmware.FirmwareService
import com.elta.android.data.features.devices.glucometer.service.info.InfoService
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class DeviceDataRepository @Inject constructor(
    private val glucometersInfoManager: GlucometersInfoManager,
    private val glucometersService: GlucometersService,
    private val firmwareService: FirmwareService,
    private val infoService: InfoService,


    private val eventsRepository: EventsRepository,
    private val eventsFromGlucometerMapper: Mapper<GlucometerEventDto, EventV2>,
    private val scanToDtoMapper: Mapper<ScanResult, GlucometerDto>,
    private val glucometerToDtoMapper: Mapper<Glucometer, GlucometerDto>,
    private val glucometerToDomainMapper: Mapper<GlucometerDto, Glucometer>,
    private val glucometerInfoToDomainMapper: Mapper<GlucometerInfoDto, GlucometerInfo>,
    override val dispatcher: CoroutineDispatcher
) : DeviceRepository, BaseRepository {

    override fun findDevices(): Observable<List<Glucometer>> =
        glucometersService.findDevices()
            .map(scanToDtoMapper::mapFromObjects)
            .map(glucometerToDomainMapper::mapFromObjects)

    override fun getDevices(): Single<List<Pair<Glucometer, GlucometerInfo>>> =
        glucometersInfoManager.getDevices()
            .map {
                it.map { glucometerWithInfo ->
                    glucometerToDomainMapper.mapFromObject(glucometerWithInfo.first) to
                            glucometerInfoToDomainMapper.mapFromObject(glucometerWithInfo.second)
                }
            }

    override fun getDevice(address: String): Single<Glucometer> =
        glucometersInfoManager.getDevice(address)
            .map(glucometerToDomainMapper::mapFromObject)

    override fun deleteDevice(address: String): Completable =
        glucometersInfoManager.deleteDevice(address)

    override fun getDeviceInfo(address: String): Single<GlucometerInfo> =
        infoService.fetchGlucometerInfo(address)
            .map(glucometerInfoToDomainMapper::mapFromObject)

    override fun getLastDeviceInfo(address: String): Single<GlucometerInfo> =
        glucometersInfoManager.getLastGlucometerInfo(address)
            .map(glucometerInfoToDomainMapper::mapFromObject)

    override fun connectDevice(device: Glucometer, pinCode: String): Completable =
        glucometersService.connectDevice(glucometerToDtoMapper.mapFromObject(device), pinCode)

    override fun syncWithDevice(device: Glucometer?): Observable<Int> =
        glucometersService.syncWithDevice(device?.let { glucometerToDtoMapper.mapFromObject(it) })
            .map(eventsFromGlucometerMapper::mapFromObjects)
            .flatMap { events ->
                eventsRepository.addEvents(events)
                    .andThen(Observable.just(events.size))
            }

    override fun updateFirmware(address: String, firmwareFile: FirmwareFile): Observable<String> =
        firmwareService.updateFirmware(address, firmwareFile)

    override fun setPrimaryDevice(address: String): Completable =
        glucometersInfoManager.setPrimaryDevice(address)

    override fun findGlucometer(address: String): Flow<Unit> =
        glucometersService.findGlucometer(address)
            .flowOn(dispatcher)
}
