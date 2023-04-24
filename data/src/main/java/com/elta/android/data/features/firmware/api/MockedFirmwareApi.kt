package com.elta.android.data.features.firmware.api

import android.content.Context
import com.elta.android.data.R
import com.elta.android.data.features.firmware.model.FirmwareNetworkResponse
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.greenrobot.essentials.io.IoUtils
import timber.log.Timber

class MockedFirmwareApi(
    private val context: Context
) : FirmwareApi {

    override fun getFirmwareInfo(): Single<FirmwareNetworkResponse> =
        Single.fromCallable {
            val stream = context.resources.openRawResource(R.raw.satellite_online_30)
            val hash = IoUtils.getMd5(stream)
            Timber.d("firmware hash: $hash")
            FirmwareNetworkResponse(
                actual = FirmwareNetworkResponse.ActualFirmware(
                    id = "Darick",
                    version = "3.0",
                    size = 0,
                    hash = hash
                ),
                compatible = null
            )
        }

    override fun downloadFirmware(version: String): Single<ResponseBody> =
        Single.fromCallable {
            val stream = context.resources.openRawResource(R.raw.satellite_online_30)
            stream.readBytes().toResponseBody("application/octet-stream".toMediaTypeOrNull())
        }

    override fun getModelFirmwareInfo(modelId: String): Single<FirmwareNetworkResponse> =
        Single.just(
            FirmwareNetworkResponse(
                actual = FirmwareNetworkResponse.ActualFirmware(
                    id = "Elyce",
                    version = "Farrah",
                    size = 3811,
                    hash = "Kanani"
                ),
                compatible = null
            )
        )

    override fun downloadModelFirmware(modelId: String, version: String): Single<ResponseBody> =
        Single.fromCallable {
            val stream = context.resources.openRawResource(R.raw.satellite_online_30)
            stream.readBytes().toResponseBody("application/octet-stream".toMediaTypeOrNull())
        }
}
