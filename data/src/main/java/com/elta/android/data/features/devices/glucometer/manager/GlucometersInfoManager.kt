package com.elta.android.data.features.devices.glucometer.manager

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.devices.cache.dto.GlucometerCachedDto
import com.elta.android.data.features.devices.cache.dto.GlucometerInfoCachedDto
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlucometersInfoManager @Inject constructor(
    private val glucometersInfoFromCacheMapper: Mapper<GlucometerInfoCachedDto, GlucometerInfoDto>,
    private val glucometerFromCacheMapper: Mapper<GlucometerCachedDto, GlucometerDto>,
    private val glucometersCache: Cache<GlucometerCachedDto>,
    private val glucometersInfoCache: Cache<GlucometerInfoCachedDto>,
) {

    fun getDevices(): Single<List<Pair<GlucometerDto, GlucometerInfoDto>>> =
        Single.just(
            glucometersCache.getAll(CommonConditions.All)
                .map(glucometerFromCacheMapper::mapFromObject)
                .map {
                    val id = it.address.hashCode().toLong()
                    it to glucometersInfoFromCacheMapper.mapFromObject(
                        glucometersInfoCache.get(CommonConditions.ById(id))
                            ?: GlucometerInfoCachedDto(id = id, secondaryId = it.address)
                    )
                }
        )

    fun getDevice(address: String): Single<GlucometerDto> =
        Single.just(glucometersCache.get(CommonConditions.ById(address.hashCode().toLong())))
            .map(glucometerFromCacheMapper::mapFromObject)

    fun deleteDevice(address: String): Completable {
        val id = address.hashCode().toLong()
        return Single.just(glucometersCache.get(CommonConditions.ById(id)))
            .doOnSuccess {
                glucometersCache.delete(CommonConditions.ById(id))
                glucometersInfoCache.delete(CommonConditions.ById(id))
            }
            .filter { it.isPrimary }
            .map { glucometersInfoCache.getAll(CommonConditions.All) }
            .filter { it.isNotEmpty() }
            .map { glucometers -> glucometers.sortedByDescending { it.syncDate }.first() }
            .map { glucometersCache.get(CommonConditions.ById(it.id)) }
            .map { it.copy(isPrimary = true) }
            .map { glucometersCache.update(listOf(it)) }
            .ignoreElement()
    }

    fun getLastGlucometerInfo(address: String): Single<GlucometerInfoDto> =
        Single.fromCallable {
            val id = address.hashCode().toLong()
            glucometersInfoCache.get(CommonConditions.ById(id))
                ?: GlucometerInfoCachedDto(id = id, secondaryId = address)
        }.map(glucometersInfoFromCacheMapper::mapFromObject)

    fun setPrimaryDevice(address: String): Completable =
        Completable.fromCallable {
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