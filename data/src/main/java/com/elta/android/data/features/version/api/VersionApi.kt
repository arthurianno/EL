package com.elta.android.data.features.version.api

import com.elta.android.data.features.version.model.AppVersionNetworkRequest
import com.elta.android.data.features.version.model.VersionResponse
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface VersionApi {
    @GET("api/apps/v1/check_version")
    fun checkAppVersion(
        @Query("appId") appId: String,
        @Query("appVersion") appVersion: String
    ): Single<VersionResponse>

    @PUT("api/telemetry/v1/appInfo")
    fun sendAppVersion(@Body request: AppVersionNetworkRequest): Completable
}
