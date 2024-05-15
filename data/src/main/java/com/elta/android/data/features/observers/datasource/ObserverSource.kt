package com.elta.android.data.features.observers.datasource

import com.elta.android.data.features.observers.model.ObserverNetworkResponse
import com.elta.android.data.features.user.dto.SimpleObserverNetworkEntity
import io.reactivex.Completable
import io.reactivex.Observable

interface ObserverSource {

    fun getObserverInvites(): Observable<List<ObserverNetworkResponse>>

    fun updateObserverName(id: String, name: String): Completable

    fun deleteObserverInvite(observables: List<SimpleObserverNetworkEntity>): Completable
}
