package com.elta.android.data.features.reports.datasource

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import okhttp3.ResponseBody
import org.greenrobot.essentials.io.IoUtils
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportFileManager @Inject constructor(
    private val context: Context
) {

    private val reportsDir: File = File(context.filesDir, "reports")

    init {
        if (!reportsDir.exists()) {
            reportsDir.mkdir()
        }
    }

    fun saveReport(name: String, extension: String, body: ResponseBody): Uri {
        val file = File(reportsDir, "$name.$extension")

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        return try {
            inputStream = body.byteStream()
            outputStream = FileOutputStream(file)

            IoUtils.copyAllBytes(inputStream, outputStream)

            Timber.d("Report saved successfully: ${file.absolutePath}, size: ${file.length()} bytes")
            getFileUri(file)
        } catch (e: IOException) {
            Timber.e(e, "Failed to save report: ${file.absolutePath}")
            throw IOException("Failed to save report file", e)
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                Timber.w(e, "Failed to close streams")
            }
        }
    }

    private fun getFileUri(file: File): Uri =
        FileProvider.getUriForFile(context, "com.elta.android.fileprovider", file)

    private fun getFileName(originName: String) = "$originName.pdf"
}
