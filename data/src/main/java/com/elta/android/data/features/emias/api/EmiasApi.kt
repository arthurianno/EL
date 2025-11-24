package com.elta.android.data.features.emias.api

import com.elta.android.data.features.emias.dto.EmiasNetworkEntity
import com.elta.android.data.features.emias.dto.EmiasStatusResponse
import io.reactivex.Completable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface EmiasApi {

    @POST("api/emias/v1/link")
    fun updateEmiasInfo(
        @Body emias: EmiasNetworkEntity
    ): Completable

    @GET("api/emias/v1/link/status")
    suspend fun getEmiasStatus() : EmiasStatusResponse

    @DELETE("api/emias/v1/link")
    suspend fun unbindEmias(): Response<Unit>
}
