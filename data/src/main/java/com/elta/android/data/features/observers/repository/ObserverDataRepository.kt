package com.elta.android.data.features.observers.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.data.common.onConnectionErrorCompletes
import com.elta.android.data.features.observers.datasource.cache.ObserverCachedSource
import com.elta.android.data.features.observers.datasource.remote.ObserverRemoteSource
import com.elta.android.data.features.observers.toDomain
import com.elta.android.data.features.user.dto.SimpleObserverNetworkEntity
import com.elta.android.domain.features.observers.model.Observer
import com.elta.android.domain.features.observers.repository.ObserverRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ObserverDataRepository @Inject constructor(
    @Cache private val cacheSource: ObserverCachedSource,
    @Remote private val remoteSource: ObserverRemoteSource
) : ObserverRepository {

    override fun getObserverInvites(): Observable<List<Observer>> =
        remoteSource.getObserverInvites()
            .flatMap { observers ->
                cacheSource.updateObservers(observers)
                    .andThen(Observable.just(observers))
            }
            .onErrorResumeNext(cacheSource.getObserverInvites())
            .map { it.toDomain() }

    override fun getObserver(id: String): Single<Observer> =
        cacheSource.getObserver(id)
            .map { it.toDomain() }

    override fun updateObserverName(id: String, name: String): Completable =
        remoteSource.updateObserverName(id, name)
            .andThen(cacheSource.updateObserverName(id, name))

    override fun sendObserverInvite(
        email: String,
        languageTag: String?,
        countryCode: String?
    ): Completable =
        remoteSource.sendObserverInvite(email, languageTag, countryCode)
            .flatMapCompletable { observer -> cacheSource.addObserver(observer) }

    override fun deleteObserverInvite(id: String): Completable =
        Single.just(listOf(SimpleObserverNetworkEntity(id)))
            .flatMapCompletable {
                cacheSource.deleteObserverInvite(it)
                    .andThen(
                        remoteSource.deleteObserverInvite(it)
                            .onConnectionErrorCompletes()
                    )
            }
}
