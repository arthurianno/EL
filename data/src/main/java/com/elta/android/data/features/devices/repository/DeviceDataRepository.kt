package com.elta.android.data.features.devices.repository

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.datasource.DeviceDataSource
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.domain.features.devices.model.Command
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.diary.events.model.Event
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class DeviceDataRepository @Inject constructor(
    private val toDomainMapper: Mapper<GlucometerDto, Glucometer>,
    private val source: DeviceDataSource
) : DeviceRepository {

    override fun findDevices(): Observable<List<Glucometer>> =
        source.findDevices().map(toDomainMapper::mapFromObjects)

    override fun getDeviceInfo(address: String, fields: List<Command>): Single<GlucometerInfo> =
        source.getGlucometerInfo(address, fields)

    override fun getDeviceEvents(): Single<List<Event>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}