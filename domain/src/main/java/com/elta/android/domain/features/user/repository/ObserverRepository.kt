package com.elta.android.domain.features.user.repository

import com.elta.android.domain.features.user.model.Observer
import io.reactivex.Completable
import io.reactivex.Observable

interface ObserverRepository {

    fun getObservers(): Observable<List<Observer>>

    fun getObserverInvites(): Observable<List<Observer>>

    fun sendObserverInvite(email: String): Completable

    fun deleteObserverInvite(id: String): Completable
}