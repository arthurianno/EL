package com.elta.android.data.features.devices.repository

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.repository.BaseRepository
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.dto.VersionDto
import com.elta.android.data.features.devices.glucometer.builder.GlucometerInfoBuilder
import com.elta.android.data.features.devices.glucometer.manager.GlucometerCacheManager
import com.elta.android.data.features.devices.mapper.GlucometerToDtoMapper
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

class DeviceInfoDataRepository @Inject constructor(
    private val glucometerCacheManager: GlucometerCacheManager,
    private val glucometerToDtoMapper: GlucometerToDtoMapper,
    private val glucometerToDomainMapper: Mapper<GlucometerDto, Glucometer>,
    private val glucometerInfoToDomainMapper: Mapper<GlucometerInfoDto, GlucometerInfo>,
    private val infoBuilder: GlucometerInfoBuilder,
    override val dispatcher: CoroutineDispatcher
) : DeviceInfoRepository, BaseRepository {

    override fun getDevices(): List<Pair<Glucometer, GlucometerInfo>> {
        return glucometerCacheManager.getDevices().map { (glucometer, info) ->
            glucometerToDomainMapper.mapFromObject(glucometer) to
                    glucometerInfoToDomainMapper.mapFromObject(info)
        }
    }


    override fun getDevice(address: String): Glucometer? {
        return glucometerCacheManager.getGlucometer(address)?.let { glucometer ->
            glucometerToDomainMapper.mapFromObject(glucometer)
        }
    }

    override fun deleteDevice(address: String) {
        glucometerCacheManager.deleteDevice(address)
    }

    override fun getLastDeviceInfo(address: String): GlucometerInfo =
        glucometerCacheManager.getLastGlucometerInfo(address).let { info ->
            glucometerInfoToDomainMapper.mapFromObject(info)
        }

    override fun getPrimaryDeviceWithLastEvent(): Pair<Glucometer, GlucometerInfo>? {
        return getDevices()
            .firstOrNull { (glucometer, _) ->
                glucometer.isPrimary
            }
    }

    override fun setPrimaryDevice(address: String) =
        glucometerCacheManager.setPrimaryDevice(address)

    override fun putDevice(glucometer: Glucometer, isPrimary: Boolean) {
        val glucometerDto = glucometerToDtoMapper.mapFromObject(glucometer)
        glucometerCacheManager.addDevice(glucometerDto, isPrimary)
    }

    override fun updateGlucometerInfo(glucometerInfo: GlucometerInfo, lastSyncedEvent: GlucometerEvent?) {
        val cacheInfo = glucometerCacheManager.getGlucometerInfo(glucometerInfo.id)

        val newInfo = infoBuilder.buildFrom( //TODO: можно сразу маппер
            id = glucometerInfo.id,
            date = glucometerInfo.deviceDate,
            temperature = glucometerInfo.temperature,
            batteryLevel = glucometerInfo.batteryLevel,
            version = VersionDto(software = glucometerInfo.softwareVersion, hardware = glucometerInfo.hardwareVersion),
            serial = glucometerInfo.glucometerSerialNumber,
            syncDate = ZonedDateTime.now(),
            lastSyncedEvent = lastSyncedEvent?.originalResponse ?: cacheInfo?.lastSyncedEvent
        )

        if (cacheInfo == null) {
            glucometerCacheManager.saveDevice(newInfo)
        } else {
            glucometerCacheManager.updateDevice(newInfo)
        }
    }
}
