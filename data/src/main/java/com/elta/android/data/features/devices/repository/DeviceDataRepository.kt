package com.elta.android.data.features.devices.repository

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.repository.BaseRepository
import com.elta.android.data.features.devices.datasource.DeviceDataSource
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.diary.events.model.Event
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
    private val eventsRepository: EventsRepository,
    private val eventsFromGlucometerMapper: Mapper<GlucometerEventDto, Event>,
    private val glucometerToDtoMapper: Mapper<Glucometer, GlucometerDto>,
    private val glucometerToDomainMapper: Mapper<GlucometerDto, Glucometer>,
    private val glucometerInfoToDomainMapper: Mapper<GlucometerInfoDto, GlucometerInfo>,
    private val source: DeviceDataSource,
    override val dispatcher: CoroutineDispatcher
) : DeviceRepository, BaseRepository {

    override fun findDevices(): Observable<List<Glucometer>> =
        source.findDevices().map(glucometerToDomainMapper::mapFromObjects)

    override fun getDevices(): Single<List<Pair<Glucometer, GlucometerInfo>>> =
        source.getDevices()
            .map {
                it.map { glucometerWithInfo ->
                    glucometerToDomainMapper.mapFromObject(glucometerWithInfo.first) to
                        glucometerInfoToDomainMapper.mapFromObject(glucometerWithInfo.second)
                }
            }

    override fun getDevice(address: String): Single<Glucometer> =
        source.getDevice(address).map(glucometerToDomainMapper::mapFromObject)

    override fun deleteDevice(address: String): Completable =
        source.deleteDevice(address)

    override fun getDeviceInfo(address: String): Single<GlucometerInfo> =
        source.getGlucometerInfo(address).map(glucometerInfoToDomainMapper::mapFromObject)

    override fun getLastDeviceInfo(address: String): Single<GlucometerInfo> =
        source.getLastGlucometerInfo(address).map(glucometerInfoToDomainMapper::mapFromObject)

    override fun getDeviceEvents(address: String): Single<List<String>> =
        source.getGlucometerEvents(address)

    override fun connectDevice(device: Glucometer, pinCode: String): Completable =
        source.connectDevice(glucometerToDtoMapper.mapFromObject(device), pinCode)

    override fun syncWithDevice(device: Glucometer?): Observable<Int> =
        source.syncWithDevice(device?.let { glucometerToDtoMapper.mapFromObject(it) })
            .map(eventsFromGlucometerMapper::mapFromObjects)
            .flatMap { events ->
                eventsRepository.addEvents(events)
                    .andThen(Observable.just(events.size))
            }

    override fun updateFirmware(address: String, firmwareFile: FirmwareFile): Observable<String> =
        source.updateFirmware(address, firmwareFile)

    override fun setPrimaryDevice(address: String): Completable =
        source.setPrimaryDevice(address)

    override fun findGlucometer(address: String): Flow<Unit> =
        source.findGlucometer(address)
            .flowOn(dispatcher)
}
