package com.elta.android.data.features.observers.datasource.remote

import com.elta.android.data.features.common.isTheLastPage
import com.elta.android.data.features.observers.api.ObserverApi
import com.elta.android.data.features.observers.model.ObserverInviteEmailNetworkRequest
import com.elta.android.data.features.observers.model.ObserverNetworkResponse
import com.elta.android.data.features.observers.model.ObserverUpdateNameNetworkRequest
import com.elta.android.data.features.observers.model.ObserversNetworkResponse
import com.elta.android.data.features.user.dto.SimpleObserverNetworkEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

private const val PAGE = 1
private const val PAGE_SIZE = 100

class ObserverRemoteDataSource @Inject constructor(
    private val api: ObserverApi
) : ObserverRemoteSource {

    override fun getObserverInvites(): Observable<List<ObserverNetworkResponse>> =
        getObserverInvitesByPage(PAGE, PAGE_SIZE)
            .map(ObserversNetworkResponse::items)

    override fun updateObserverName(id: String, name: String) =
        api.updateObserverName(id, ObserverUpdateNameNetworkRequest(name))

    override fun sendObserverInvite(email: String): Single<ObserverNetworkResponse> =
        api.sendObserverInvite(ObserverInviteEmailNetworkRequest(email))

    override fun deleteObserverInvite(observables: List<SimpleObserverNetworkEntity>): Completable =
        api.deleteObserverInvite(observables.first().id)

    private fun getObserverInvitesByPage(
        page: Int,
        size: Int
    ): Observable<ObserversNetworkResponse> =
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
            .collectInto(mutableListOf<ObserversNetworkResponse>()) { list, data ->
                list.add(data)
            }
            .map { list ->
                val allData = list.map { it.items }.flatten()
                val lastMeta = list.last().meta
                ObserversNetworkResponse(allData, lastMeta)
            }
            .toObservable()
}
