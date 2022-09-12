package com.elta.android.data.features.firmware.api

import com.elta.android.data.features.firmware.dto.FirmwareDto
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface FirmwareApi {

    @GET("api/glucometer/v1/firmwares")
    fun getFirmwareInfo(): Single<FirmwareDto>

    @GET("api/glucometer/v1/firmwares/v{version}")
    fun downloadFirmware(@Path("version") version: String): Single<ResponseBody>
}
