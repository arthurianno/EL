package com.elta.android.data.features.firmware.api

import android.content.Context
import com.elta.android.data.features.firmware.dto.ActualFirmwareDto
import com.elta.android.data.features.firmware.dto.FirmwareDto
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream

class MockedFirmwareApi(
    private val context: Context
) : FirmwareApi {

    override fun getFirmwareInfo(): Single<FirmwareDto> =
        Single.just(
            FirmwareDto(
                actual = ActualFirmwareDto(
                    version = "1.6",
                    size = 0,
                    hash = ""
                ),
                compatible = "1.6"
            )
        )

    override fun getFirmware(version: String): Single<ResponseBody> =
        Single.fromCallable {
            val path = "android.resource://" + context.packageName + "/" + com.elta.android.data.R.raw.satellite_online_16
            val file = File(path)
            val dis = DataInputStream(FileInputStream(file))
            val data = ByteArray(file.length().toInt())
            dis.readFully(data)
            dis.close()
            ResponseBody.create(MediaType.parse("application/octet-stream"), data)
        }
}