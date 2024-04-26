package com.elta.android.data.features.glucometers.repository

import com.elta.android.data.features.glucometers.api.GlucometersApi
import com.elta.android.data.features.glucometers.mapper.toNM
import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import com.elta.android.domain.features.glucometers.repository.GlucometersRepository
import io.reactivex.Completable
import javax.inject.Inject

class GlucometersDataRepository @Inject constructor(
    private val glucometersApi: GlucometersApi,
    private val deviceRepository: DeviceInfoRepository,
) : GlucometersRepository {

    override fun sync(): Completable {
        val devices = deviceRepository.getDevices()
        return glucometersApi.putGlucometers(devices.toNM())
    }
}
