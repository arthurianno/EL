package com.elta.android.data.features.firmware.api

import android.content.Context
import com.elta.android.data.R
import com.elta.android.data.features.firmware.model.NewVersionFirmwareInfoResponse
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody

class MockedFirmwareApi(
    private val context: Context
) : FirmwareApi {


    override fun downloadModelFirmware(id: String): Single<ResponseBody> {
        return Single.fromCallable {
            val stream = context.resources.openRawResource(R.raw.satellite_online_30)
            stream.readBytes().toResponseBody("application/octet-stream".toMediaTypeOrNull())
        }
    }

    override fun getFirmwareInfo(
        mac: String,
        serialNumber: String?,
        hardwareVersion: String?,
        firmwareVersion: String?
    ): Single<NewVersionFirmwareInfoResponse> {
        return Single.just(
            NewVersionFirmwareInfoResponse(
                id = "d1slpac01d",
                version = "123",
                size = 123,
                hash = "12r12[aslc=zxkxas=cz=xaxz-zcalzc"
            )
        )
    }
}
