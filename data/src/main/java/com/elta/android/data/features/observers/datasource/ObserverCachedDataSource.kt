package com.elta.android.data.features.observers.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.observers.cache.dto.ObserverCacheDto
import com.elta.android.data.features.observers.dto.ObserverDto
import com.elta.android.data.features.user.dto.SimpleObserverDto
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class ObserverCachedDataSource @Inject constructor(
    private val fromCacheMapper: Mapper<ObserverCacheDto, ObserverDto>,
    private val cache: Cache<ObserverCacheDto>
) : ObserverDataSource {

    override fun getObserverInvites(): Observable<List<ObserverDto>> =
        Observable.fromCallable {
            cache.getAll(CommonConditions.All)
        }.map(fromCacheMapper::mapFromObjects)

    override fun sendObserverInvite(email: String): Completable =
        Completable.error(Throwable("Unsupported cache method"))

    override fun deleteObserverInvite(observables: List<SimpleObserverDto>): Completable =
        Completable.fromCallable {
            cache.delete(CommonConditions.ByIds(observables.map { it.id.hashCode().toLong() }))
        }
}