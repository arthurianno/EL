package com.elta.android.data.features.firmware.api

import com.elta.android.data.features.firmware.model.FirmwareNetworkResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface FirmwareApi {

    @GET("api/glucometer/v1/firmwares")
    fun getFirmwareInfo(): Single<FirmwareNetworkResponse>

    @GET("api/glucometer/v1/firmwares/v{version}")
    fun downloadFirmware(@Path("version") version: String): Single<ResponseBody>

    @GET("api/glucometer/v1/{glucometerModelId}/firmwares")
    fun getModelFirmwareInfo(@Path("glucometerModelId") modelId: String): Single<FirmwareNetworkResponse>

    @GET("api/glucometer/v1/{glucometerModelId}/firmwares/v{version}")
    fun downloadModelFirmware(
        @Path("glucometerModelId") modelId: String,
        @Path("version") version: String
    ): Single<ResponseBody>
}
