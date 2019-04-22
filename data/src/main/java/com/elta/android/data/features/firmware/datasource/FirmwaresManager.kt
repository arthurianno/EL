package com.elta.android.data.features.firmware.datasource

import android.content.Context
import okhttp3.ResponseBody
import org.greenrobot.essentials.io.IoUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirmwaresManager @Inject constructor(context: Context) {

    private val firmwares: File = File(context.filesDir, "firmwares")

    init {
        if (!firmwares.exists()) {
            firmwares.mkdir()
        }
    }

    fun writeToFile(version: String, body: ResponseBody): File? =
        try {
            val file = File(firmwares, "satellite_online_${version.replace(".", "")}.zip")

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)

                IoUtils.copyAllBytes(inputStream, outputStream)

                file
            } catch (e: IOException) {
                null
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            null
        }
}