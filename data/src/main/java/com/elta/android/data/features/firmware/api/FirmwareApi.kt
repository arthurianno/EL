package com.elta.android.data.features.firmware.api

import com.elta.android.data.features.firmware.model.NewVersionFirmwareInfoResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FirmwareApi {

    @GET("api/glucometer/v1/firmwares/{id}")
    fun downloadModelFirmware(
        @Path("id") id: String
    ): Single<ResponseBody>

    @GET("api/glucometer/v1/firmwares/new-version")
    fun getFirmwareInfo(
        @Query("mac") mac: String,
        @Query("serialNumber") serialNumber: String?,
        @Query("hardwareVersion") hardwareVersion: String?,
        @Query("firmwareVersion") firmwareVersion: String?,
    ): Single<NewVersionFirmwareInfoResponse>
}
