package com.elta.android.data.features.firmware.api

import android.content.Context
import com.elta.android.data.R
import com.elta.android.data.features.firmware.dto.ActualFirmwareDto
import com.elta.android.data.features.firmware.dto.FirmwareDto
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.greenrobot.essentials.io.IoUtils
import timber.log.Timber

class MockedFirmwareApi(
    private val context: Context
) : FirmwareApi {

    override fun getFirmwareInfo(): Single<FirmwareDto> =
        Single.fromCallable {
            val stream = context.resources.openRawResource(R.raw.satellite_online_30)
            val hash = IoUtils.getMd5(stream)
            Timber.d("firmware hash: $hash")
            FirmwareDto(
                actual = ActualFirmwareDto(
                    version = "3.0",
                    size = 0,
                    hash = hash
                ),
                compatible = "3.0"
            )
        }

    override fun downloadFirmware(version: String): Single<ResponseBody> =
        Single.fromCallable {
            val stream = context.resources.openRawResource(R.raw.satellite_online_30)
            ResponseBody.create(MediaType.parse("application/octet-stream"), stream.readBytes())
        }
}
