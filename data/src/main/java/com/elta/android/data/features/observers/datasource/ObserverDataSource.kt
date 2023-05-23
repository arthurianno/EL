package com.elta.android.data.features.observers.datasource

import com.elta.android.data.features.observers.model.ObserverNetworkResponse
import com.elta.android.data.features.user.dto.SimpleObserverNetworkEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface ObserverDataSource {

    fun getObserverInvites(): Observable<List<ObserverNetworkResponse>>

    fun getObserver(id: String): Single<ObserverNetworkResponse>

    fun sendObserverInvite(email: String): Completable

    fun updateObserverName(id: String, name: String): Completable

    fun deleteObserverInvite(observables: List<SimpleObserverNetworkEntity>): Completable
}
