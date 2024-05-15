package com.elta.android.data.features.observers.datasource.cache

import com.elta.android.data.features.observers.datasource.ObserverSource
import com.elta.android.data.features.observers.model.ObserverNetworkResponse
import io.reactivex.Completable
import io.reactivex.Single

interface ObserverCachedSource : ObserverSource {

    fun getObserver(id: String): Single<ObserverNetworkResponse>

    fun updateObservers(items: List<ObserverNetworkResponse>): Completable

    fun addObserver(item: ObserverNetworkResponse): Completable
}
