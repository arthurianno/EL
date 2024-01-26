package com.elta.android.data.features.devices.glucometer.manager

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.devices.cache.dto.GlucometerCachedDto
import com.elta.android.data.features.devices.cache.dto.GlucometerInfoCachedDto
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.mapper.GlucometerToCacheMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlucometerCacheManager @Inject constructor(
    private val glucometersInfoFromCacheMapper: Mapper<GlucometerInfoCachedDto, GlucometerInfoDto>,
    private val glucometersInfoToCacheMapper: Mapper<GlucometerInfoDto, GlucometerInfoCachedDto>,
    private val glucometerFromCacheMapper: Mapper<GlucometerCachedDto, GlucometerDto>,
    private val glucometerToCacheMapper: GlucometerToCacheMapper,
    private val glucometersCache: Cache<GlucometerCachedDto>,
    private val glucometersInfoCache: Cache<GlucometerInfoCachedDto>,
) {

    fun getDevices(): List<Pair<GlucometerDto, GlucometerInfoDto>> =
        glucometersCache.getAll(CommonConditions.All)
            .map(glucometerFromCacheMapper::mapFromObject)
            .map {
                val id = it.address.hashCode().toLong()
                it to glucometersInfoFromCacheMapper.mapFromObject(
                    glucometersInfoCache.get(CommonConditions.ById(id))
                        ?: GlucometerInfoCachedDto(id = id, secondaryId = it.address)
                )
            }

    fun getGlucometerInfo(address: String): GlucometerInfoDto? {
        return glucometersInfoCache.get(CommonConditions.ById(address.hashCode().toLong()))
            ?.let { info ->
                glucometersInfoFromCacheMapper.mapFromObject(info)
            }
    }

    fun saveDevice(glucometerInfo: GlucometerInfoDto) {
        val info = glucometersInfoToCacheMapper.mapFromObject(glucometerInfo)
        glucometersInfoCache.add(listOf(info))
    }

    fun updateDevice(glucometerInfo: GlucometerInfoDto) {
        val info = glucometersInfoToCacheMapper.mapFromObject(glucometerInfo)
        glucometersInfoCache.update(listOf(info))
    }

    fun deleteDevice(address: String) {
        val id = address.hashCode().toLong()
        val cacheGlucometer = glucometersCache.get(CommonConditions.ById(id))
        if (cacheGlucometer != null) {
            glucometersCache.delete(CommonConditions.ById(id))
            glucometersInfoCache.delete(CommonConditions.ById(id))
            if (cacheGlucometer.isPrimary) {
                val newGlucometerInfo = glucometersInfoCache.getAll(CommonConditions.All)
                    .sortedByDescending { it.syncDate }
                    .firstOrNull()
                if (newGlucometerInfo != null) {
                    glucometersCache.get(CommonConditions.ById(newGlucometerInfo.id))
                        ?.copy(isPrimary = true)
                        ?.let { newGlucometer ->
                            glucometersCache.update(listOf(newGlucometer))
                        }

                }

            }
        }
    }

    fun getLastGlucometerInfo(address: String): GlucometerInfoDto {
        val id = address.hashCode().toLong()
        val info = glucometersInfoCache.get(CommonConditions.ById(id)) ?: GlucometerInfoCachedDto(
            id = id,
            secondaryId = address
        )
        return glucometersInfoFromCacheMapper.mapFromObject(info)
    }

    fun getGlucometer(address: String): GlucometerDto? {
        return glucometersCache.get(CommonConditions.ById(address.hashCode().toLong()))
            ?.let { info ->
                glucometerFromCacheMapper.mapFromObject(info)
            }
    }

    fun addDevice(glucometerDto: GlucometerDto, isPrimary: Boolean) {
        val cache = glucometerToCacheMapper.mapFromObject(glucometerDto).apply { this.isPrimary = isPrimary }
        glucometersCache.add(listOf(cache))
    }

    fun setPrimaryDevice(address: String) {
        val glucometers = glucometersCache.getAll(CommonConditions.All)
        var oldPrimaryGlucometer: GlucometerCachedDto? = null
        var newPrimaryGlucometer: GlucometerCachedDto? = null
        glucometers.forEach {
            when {
                it.isPrimary -> oldPrimaryGlucometer = it.copy(isPrimary = false)
                it.address == address -> newPrimaryGlucometer = it.copy(isPrimary = true)
            }
        }
        val glucometersToUpdate = mutableListOf<GlucometerCachedDto>().apply {
            oldPrimaryGlucometer?.let { add(it) }
            newPrimaryGlucometer?.let { add(it) }
        }.toList()
        if (glucometersToUpdate.isNotEmpty()) glucometersCache.update(glucometersToUpdate)
    }

}