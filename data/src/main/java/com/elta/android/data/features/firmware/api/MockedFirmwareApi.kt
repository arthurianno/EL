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
            val stream = context.resources.openRawResource(R.raw.satellite_online_403)
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
                id = "047303d8-6a31-42c9-bd41-fa8ec58d75a5",
                version = "4.0.3",
                size = 43499,
                hash = "873B2329ACD1BD802086D7FCEC8EB668"
            )
        )
    }
}
