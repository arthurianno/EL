package com.elta.android.data.features.reports.datasource

import android.content.Context
import android.net.Uri
import android.support.v4.content.FileProvider
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
class ReportFileManager @Inject constructor(
    private val context: Context
) {

    private val reportsDir: File = File(context.filesDir, "reports")

    init {
        if (!reportsDir.exists()) {
            reportsDir.mkdir()
        }
    }

    fun saveReport(name: String, body: ResponseBody): Uri =
        try {
            val file = File(reportsDir, getFileName(name))

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)

                IoUtils.copyAllBytes(inputStream, outputStream)

                getFileUri(file)
            } catch (e: IOException) {
                Uri.EMPTY
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            Uri.EMPTY
        }

    private fun getFileUri(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    private fun getFileName(originName: String) = "$originName.pdf"
}