package com.elta.android.data.features.firmware.datasource

import android.content.Context
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileManager @Inject constructor(context: Context) {

    private val dir: File = File(context.cacheDir, "firmwares")

    init {
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    fun writeToFile(version: String, body: ResponseBody): File? =
        try {
            val file = File(dir, "firmware_$version")

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val reader = ByteArray(4096)

                val fileSize: Long = body.contentLength()
                var downloaded: Long = 0

                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)

                while (true) {
                    val read: Int = inputStream?.read() ?: -1

                    if (read == -1) {
                        break
                    }

                    outputStream.write(reader, 0, read)

                    downloaded += read

                    Timber.d("downloaded: $downloaded of $fileSize")
                }

                outputStream.flush()

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