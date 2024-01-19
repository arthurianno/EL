package com.elta.android.data.features.devices.repository

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.repository.BaseRepository
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.devices.cache.GlucometersConditions
import com.elta.android.data.features.devices.cache.dto.GlucometerCachedDto
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.repository.GlucometerRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GlucometerDataRepository @Inject constructor(
    private val glucometersCache: Cache<GlucometerCachedDto>,
    //TODO: слишком много мапперов
    private val glucometerFromCacheMapper: Mapper<GlucometerCachedDto, GlucometerDto>,
    private val glucometerToDomainMapper: Mapper<GlucometerDto, Glucometer>,
    //TODO: слишком много мапперов
    private val glucometerToDtoMapper: Mapper<Glucometer, GlucometerDto>,
    private val glucometerToCacheMapper: Mapper<GlucometerDto, GlucometerCachedDto>,

    override val dispatcher: CoroutineDispatcher
) : GlucometerRepository, BaseRepository {

    override fun getPrimaryDevice(): Glucometer? {
        val primaryDeviceFromCache = glucometersCache.get(GlucometersConditions.Primary)
        return primaryDeviceFromCache?.let {
            val dto = glucometerFromCacheMapper.mapFromObject(it)
            glucometerToDomainMapper.mapFromObject(dto)
        }
    }

    override fun putDevice(glucometer: Glucometer, isPrimary: Boolean) {
        val dto = glucometerToDtoMapper.mapFromObject(glucometer)
        val cache = glucometerToCacheMapper.mapFromObject(dto).apply { this.isPrimary = isPrimary }
        glucometersCache.add(listOf(cache))
    }
}