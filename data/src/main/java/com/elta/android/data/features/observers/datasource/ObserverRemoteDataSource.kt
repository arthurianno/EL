package com.elta.android.data.features.observers.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.updateCache
import com.elta.android.data.features.common.isTheLastPage
import com.elta.android.data.features.observers.api.ObserverApi
import com.elta.android.data.features.observers.cache.dto.ObserverCacheDto
import com.elta.android.data.features.observers.dto.ObserverDto
import com.elta.android.data.features.observers.dto.ObserverInviteEmailRequest
import com.elta.android.data.features.observers.dto.ObserverUpdateNameRequest
import com.elta.android.data.features.observers.dto.ObserversQueryResultDto
import com.elta.android.data.features.user.dto.SimpleObserverDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ObserverRemoteDataSource @Inject constructor(
    private val mapper: Mapper<ObserverDto, ObserverCacheDto>,
    private val cache: Cache<ObserverCacheDto>,
    private val api: ObserverApi
) : ObserverDataSource {

    override fun getObserverInvites(): Observable<List<ObserverDto>> =
        getObserverInvitesByPage(PAGE, PAGE_SIZE)
            .map(ObserversQueryResultDto::items)
            .doOnNext { events -> updateCache(events, cache, mapper) }

    override fun getObserver(id: String): Single<ObserverDto> =
        Single.error(IllegalStateException("getObserver method dose not support by remote data source"))

    override fun updateObserverName(id: String, name: String) =
        api.updateObserverName(id, ObserverUpdateNameRequest(name))

    override fun sendObserverInvite(email: String): Completable =
        api.sendObserverInvite(ObserverInviteEmailRequest(email))
            .doOnSuccess { cache.add(listOf(mapper.mapFromObject(it))) }
            .ignoreElement()

    override fun deleteObserverInvite(observables: List<SimpleObserverDto>): Completable =
        api.deleteObserverInvite(observables.first().id)

    private fun getObserverInvitesByPage(
        page: Int,
        size: Int
    ): Observable<ObserversQueryResultDto> =
        api.getObserverInvites(page, size)
            .concatMap { data ->
                val meta = data.meta
                val nextPage = meta.currentPage + 1
                when (meta.isTheLastPage()) {
                    true -> Observable.just(data)
                    else -> Observable.just(data).concatWith(
                        getObserverInvitesByPage(
                            nextPage,
                            meta.pageSize
                        )
                    )
                }
            }
            .collectInto(mutableListOf<ObserversQueryResultDto>()) { list, data ->
                list.add(data)
            }
            .map { list ->
                val allData = list.map { it.items }.flatten()
                val lastMeta = list.last().meta
                ObserversQueryResultDto(allData, lastMeta)
            }
            .toObservable()

    private companion object {
        const val PAGE = 1
        const val PAGE_SIZE = 100
    }
}
