package com.elta.android.domain.features.observers.repository

import com.elta.android.domain.features.observers.model.Observer
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface ObserverRepository {

    fun getObserverInvites(): Observable<List<Observer>>

    fun getObserver(id: String): Single<Observer>

    fun updateObserverName(id: String, name: String): Completable

    fun sendObserverInvite(
        email: String,
        languageTag: String? = null,
        countryCode: String? = null
    ): Completable

    fun deleteObserverInvite(id: String): Completable
}
