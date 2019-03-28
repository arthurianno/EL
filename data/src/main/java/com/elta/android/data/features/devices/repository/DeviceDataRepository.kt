package com.elta.android.data.features.devices.repository

import com.elta.android.data.features.devices.datasource.DeviceDataSource
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.repository.DeviceRepository
import io.reactivex.Observable
import javax.inject.Inject

class DeviceDataRepository @Inject constructor(
    private val source: DeviceDataSource
) : DeviceRepository {

    override fun findDevices(): Observable<List<Glucometer>> =
        source.findDevices().map {
            it.map {
                Glucometer(
                    id = it.id,
                    address = it.address,
                    name = it.name
                )
            }
        }
}