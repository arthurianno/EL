package com.elta.android.data.features.observers.api

import com.elta.android.data.features.observers.dto.ObserverDto
import com.elta.android.data.features.observers.dto.ObserverInviteEmailRequest
import com.elta.android.data.features.observers.dto.ObserversQueryResultDto
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
    ): Observable<ObserversQueryResultDto>

    @PUT("api/share/v1/observers/invites")
    fun sendObserverInvite(@Body email: ObserverInviteEmailRequest): Single<ObserverDto>

    @DELETE("api/share/v1/observers/invites/{id}")
    fun deleteObserverInvite(@Path("id") id: String): Completable
}