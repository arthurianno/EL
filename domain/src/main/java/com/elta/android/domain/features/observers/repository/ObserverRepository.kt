package com.elta.android.domain.features.observers.repository

import com.elta.android.domain.features.observers.model.Observer
import io.reactivex.Completable
import io.reactivex.Observable

interface ObserverRepository {

    fun getObserverInvites(): Observable<List<Observer>>

    fun sendObserverInvite(email: String): Completable

    fun deleteObserverInvite(id: String): Completable
}