package com.elta.android.data.features.observers.datasource.remote

import com.elta.android.data.features.observers.datasource.ObserverSource
import com.elta.android.data.features.observers.model.ObserverNetworkResponse
import io.reactivex.Single

interface ObserverRemoteSource : ObserverSource {

    fun sendObserverInvite(email: String): Single<ObserverNetworkResponse>
}
