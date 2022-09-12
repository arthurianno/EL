package com.elta.android.data.features.observers.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.observers.cache.dto.ObserverCacheDto
import com.elta.android.data.features.observers.dto.ObserverDto
import com.elta.android.data.features.user.dto.SimpleObserverDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ObserverCachedDataSource @Inject constructor(
    private val fromCacheMapper: Mapper<ObserverCacheDto, ObserverDto>,
    private val cache: Cache<ObserverCacheDto>
) : ObserverDataSource {

    override fun getObserverInvites(): Observable<List<ObserverDto>> =
        Observable.fromCallable {
            cache.getAll(CommonConditions.All)
        }.map(fromCacheMapper::mapFromObjects)

    override fun getObserver(id: String): Single<ObserverDto> =
        Single.fromCallable {
            cache.get(CommonConditions.ById(id.hashCode().toLong()))
        }.map(fromCacheMapper::mapFromObject)

    override fun updateObserverName(id: String, name: String): Completable =
        Completable.fromCallable {
            val observer = cache.get(CommonConditions.ById(id.hashCode().toLong()))
            observer?.let {
                cache.update(arrayListOf(it.copy(name = name)))
            }
        }

    override fun sendObserverInvite(email: String): Completable =
        Completable.error(Throwable("Unsupported cache method"))

    override fun deleteObserverInvite(observables: List<SimpleObserverDto>): Completable =
        Completable.fromCallable {
            cache.delete(CommonConditions.ByIds(observables.map { it.id.hashCode().toLong() }))
        }
}
