@file:Suppress("SwallowedException")

package com.elta.android.data.features.firmware.datasource

import android.content.Context
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import okhttp3.ResponseBody
import org.greenrobot.essentials.io.IoUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

// TODO После мержа с релизом 2 перенести методы данного класса в FileStorage (общий класс для работы с файловой системой)
@Singleton
class FirmwaresManager @Inject constructor(
    private val crashlyticsReport: CrashlyticsReport,
    context: Context
) {

    private val firmwares: File = File(context.filesDir, "firmwares")

    init {
        if (!firmwares.exists()) {
            firmwares.mkdir()
        }
    }

    fun writeToFile(version: String, body: ResponseBody): File? =
        try {
            val file = File(firmwares, getZipName(version))

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

    fun getFile(name: String, isZipFile: Boolean): File? =
        try {
            val fileName = if (isZipFile) getZipName(name) else name
            val file = File(firmwares, fileName)
            if (file.exists()) file else null
        } catch (e: IOException) {
            null
        }

    /**
     * Распаковываем zip-архив и сохраняем файлы в директорию.
     * @param path путь к файлу.
     * @return список названий файлов.
     */
    fun unpackZip(path: String): List<String> {
        val fileNames = mutableListOf<String>()

        try {
            val file = File(path)
            if (file.exists()) {
                ZipFile(file).use { zip ->
                    zip.entries().asSequence().forEach { entry ->
                        zip.getInputStream(entry).use { input ->
                            val fileName = entry.name

                            if (!entry.isDirectory) {
                                fileNames.add(fileName)
                                extractFile(input, fileName)
                            }
                        }
                    }
                }
            }

        } catch (ex: IOException) {
            crashlyticsReport.log("Unpacking zip finished with error: $ex")
        }
        return fileNames
    }

    @Throws(IOException::class)
    private fun extractFile(inputStream: InputStream, fileName: String) {
        val outputStream = FileOutputStream("${firmwares.path}$DASH_SYMBOL${fileName}")

        outputStream.use { output ->
            IoUtils.copyAllBytes(inputStream, output)
        }
    }

    private fun getZipName(version: String): String =
        "satellite_online_${version.replace(".", "")}.zip"
}

private const val DASH_SYMBOL = "/"
