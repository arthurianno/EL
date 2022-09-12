package com.elta.android.data.features.observers.datasource

import com.elta.android.data.features.observers.dto.ObserverDto
import com.elta.android.data.features.user.dto.SimpleObserverDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface ObserverDataSource {

    fun getObserverInvites(): Observable<List<ObserverDto>>

    fun getObserver(id: String): Single<ObserverDto>

    fun sendObserverInvite(email: String): Completable

    fun updateObserverName(id: String, name: String): Completable

    fun deleteObserverInvite(observables: List<SimpleObserverDto>): Completable
}
