package com.elta.android.data.features.observers.datasource.cache

import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.observers.model.ObserverDbEntity
import com.elta.android.data.features.observers.model.ObserverNetworkResponse
import com.elta.android.data.features.observers.toDb
import com.elta.android.data.features.observers.toNetwork
import com.elta.android.data.features.user.dto.SimpleObserverNetworkEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ObserverCachedDataSource @Inject constructor(
    private val cache: Cache<ObserverDbEntity>
) : ObserverCachedSource {

    override fun getObserverInvites(): Observable<List<ObserverNetworkResponse>> =
        Observable.fromCallable {
            cache.getAll(CommonConditions.All)
        }.map { it.toNetwork() }

    override fun getObserver(id: String): Single<ObserverNetworkResponse> =
        Single.fromCallable {
            cache.get(CommonConditions.ById(id.hashCode().toLong()))
        }.map { it.toNetwork() }

    override fun updateObserverName(id: String, name: String): Completable =
        Completable.fromCallable {
            val observer = cache.get(CommonConditions.ById(id.hashCode().toLong()))
            observer?.let {
                cache.update(arrayListOf(it.copy(name = name)))
            }
        }

    override fun deleteObserverInvite(observables: List<SimpleObserverNetworkEntity>): Completable =
        Completable.fromCallable {
            cache.delete(CommonConditions.ByIds(observables.map { it.id.hashCode().toLong() }))
        }

    override fun updateObservers(items: List<ObserverNetworkResponse>): Completable =
        Completable.fromCallable {
            cache.delete(CommonConditions.All)
            cache.add(
                items.map(ObserverNetworkResponse::toDb)
            )
        }

    override fun addObserver(item: ObserverNetworkResponse): Completable {
        return Completable.fromCallable {
            cache.add(listOf(item.toDb()))
        }
    }
}
