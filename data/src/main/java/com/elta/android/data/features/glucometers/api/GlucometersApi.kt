package com.elta.android.data.features.glucometers.api

import com.elta.android.data.features.glucometers.dto.GlucometerNetworkEntity
import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.PUT

interface GlucometersApi {

    @PUT("api/profile/v1/glucometers")
    fun putGlucometers(
        @Body glucometers: GlucometerNetworkEntity
    ): Completable

}