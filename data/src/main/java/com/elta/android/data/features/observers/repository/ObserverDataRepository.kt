package com.elta.android.data.features.observers.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.common.onConnectionErrorCompletes
import com.elta.android.data.common.onConnectionErrorReturnsEmpty
import com.elta.android.data.features.observers.datasource.ObserverDataSource
import com.elta.android.data.features.observers.dto.ObserverDto
import com.elta.android.data.features.user.dto.SimpleObserverDto
import com.elta.android.domain.features.observers.model.Observer
import com.elta.android.domain.features.observers.repository.ObserverRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ObserverDataRepository @Inject constructor(
    private val toDomainMapper: Mapper<ObserverDto, Observer>,
    @Cache private val cacheSource: ObserverDataSource,
    @Remote private val remoteSource: ObserverDataSource
) : ObserverRepository {

    override fun getObserverInvites(): Observable<List<Observer>> =
        remoteSource.getObserverInvites()
            .onConnectionErrorReturnsEmpty()
            .flatMap { cacheSource.getObserverInvites() }
            .map(toDomainMapper::mapFromObjects)

    override fun getObserver(id: String): Single<Observer> =
        cacheSource.getObserver(id)
            .map(toDomainMapper::mapFromObject)

    override fun updateObserverName(id: String, name: String): Completable =
        remoteSource.updateObserverName(id, name)
            .andThen(cacheSource.updateObserverName(id, name))

    override fun sendObserverInvite(email: String): Completable =
        remoteSource.sendObserverInvite(email)

    override fun deleteObserverInvite(id: String): Completable =
        Single.just(listOf(SimpleObserverDto(id)))
            .flatMapCompletable {
                cacheSource.deleteObserverInvite(it)
                    .andThen(
                        remoteSource.deleteObserverInvite(it)
                            .onConnectionErrorCompletes()
                    )
            }
}
