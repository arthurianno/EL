@file:Suppress("SwallowedException")

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

// TODO После мержа с релизом 2 перенести методы данного класса в FileStorage (общий класс для работы с файловой системой)
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
            val file = File(firmwares, getFileName(version))

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

    fun getFile(version: String): File? =
        try {
            val file = File(firmwares, getFileName(version))
            if (file.exists()) file else null
        } catch (e: IOException) {
            null
        }

    private fun getFileName(version: String): String = "satellite_online_${version.replace(".", "")}.zip"
}
