package com.elta.android.data.features.observers.datasource

import com.elta.android.data.features.observers.dto.ObserverDto
import com.elta.android.data.features.user.dto.SimpleObserverDto
import io.reactivex.Completable
import io.reactivex.Observable

interface ObserverDataSource {

    fun getObserverInvites(): Observable<List<ObserverDto>>

    fun sendObserverInvite(email: String): Completable

    fun deleteObserverInvite(observables: List<SimpleObserverDto>): Completable
}