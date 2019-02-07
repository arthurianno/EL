package com.elta.android.data.features.sale_points.datasource

import com.elta.android.data.common.checkNetwork
import com.elta.android.data.features.sale_points.api.SalePointsApi
import com.elta.android.data.features.sale_points.dto.MetaDto
import com.elta.android.data.features.sale_points.dto.SalePointDto
import com.elta.android.data.features.sale_points.dto.SalePointsDto
import com.elta.android.data.features.sale_points.storage.SyncStorage
import com.nullgr.core.hardware.NetworkChecker
import io.reactivex.Observable
import javax.inject.Inject

class SalePointsRemoteDataSource @Inject constructor(
    private val syncStorage: SyncStorage,
    private val checker: NetworkChecker,
    private val api: SalePointsApi
) : SalePointsDataSource {

    override fun getSalePoints(): Observable<List<SalePointDto>> =
        getSalePointsByPage(PAGE, PAGE_SIZE).checkNetwork(checker)
            .doOnNext { syncStorage.lastSync = System.currentTimeMillis() }
            .map(SalePointsDto::points)

    private fun getSalePointsByPage(page: Int, size: Int): Observable<SalePointsDto> =
        api.getSalePoints(syncStorage.lastSync, page, size)
            .switchMap { points ->
                val meta = points.meta
                val nextPage = meta.currentPage + 1
                when (meta.isTheLastPage()) {
                    true -> Observable.just(points)
                    else -> Observable.just(points).concatWith(getSalePointsByPage(nextPage, meta.pageSize))
                }
            }
            .collectInto(mutableListOf<SalePointsDto>()) { list, points -> list.add(points) }
            .map { list ->
                val allPoints = list.map { it.points }.flatten()
                val lastMeta = list.last().meta
                SalePointsDto(allPoints, lastMeta)
            }
            .toObservable()

    private fun MetaDto.isTheLastPage(): Boolean = currentPage * pageSize >= totalItems

    private companion object {
        const val PAGE = 1
        const val PAGE_SIZE = 100
    }
}