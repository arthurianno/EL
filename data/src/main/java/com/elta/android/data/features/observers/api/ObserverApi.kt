package com.elta.android.data.features.observers.api

import com.elta.android.data.features.observers.model.ObserverInviteEmailNetworkRequest
import com.elta.android.data.features.observers.model.ObserverNetworkResponse
import com.elta.android.data.features.observers.model.ObserverUpdateNameNetworkRequest
import com.elta.android.data.features.observers.model.ObserversNetworkResponse
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ObserverApi {

    @GET("api/share/v1/observers/invites")
    fun getObserverInvites(
        @Query("pageIndex") page: Int,
        @Query("pageSize") pageSize: Int
    ): Observable<ObserversNetworkResponse>

    @PUT("api/share/v1/observers/invites")
    fun sendObserverInvite(@Body email: ObserverInviteEmailNetworkRequest): Single<ObserverNetworkResponse>

    @PUT("api/share/v1/observers/{id}")
    fun updateObserverName(
        @Path("id") id: String,
        @Body name: ObserverUpdateNameNetworkRequest
    ): Completable

    @DELETE("api/share/v1/observers/invites/{id}")
    fun deleteObserverInvite(@Path("id") id: String): Completable
}
