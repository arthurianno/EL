package com.elta.android.data.features.sale_points.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.sale_points.cache.SalePointsCache
import com.elta.android.data.features.sale_points.cache.dto.SalePointCacheDto
import com.elta.android.data.features.sale_points.dto.SalePointDto
import io.reactivex.Observable
import javax.inject.Inject

class SalePointsCachedDataSource @Inject constructor(
    private val fromCacheMapper: Mapper<SalePointCacheDto, SalePointDto>,
    private val cache: SalePointsCache
) : SalePointsDataSource {

    override fun getSalePoints(): Observable<List<SalePointDto>> =
        Observable.fromCallable { cache.getAll() }.map(fromCacheMapper::mapFromObjects)
}