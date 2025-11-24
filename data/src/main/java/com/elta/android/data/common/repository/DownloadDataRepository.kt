package com.elta.android.data.common.repository

import android.content.Context
import com.elta.android.data.common.datasource.download.DownloadSource
import com.elta.android.domain.common.model.FileDirectory
import com.elta.android.domain.common.repository.DownloadRepository
import org.greenrobot.essentials.io.IoUtils
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class DownloadDataRepository @Inject constructor(
    private val source: DownloadSource,
    private val context: Context
) : DownloadRepository {

    private val pdfDirectory = File(context.filesDir, FileDirectory.Document.path)

    init {
        if (!pdfDirectory.exists()) {
            pdfDirectory.mkdir()
        }
    }

    override suspend fun downloadFile(
        url: String,
        destinationDirectory: FileDirectory,
        destinationFile: String
    ): File {
        val target = File(File(context.filesDir, destinationDirectory.path), destinationFile)

        source.download(url).use { inputStream ->
            FileOutputStream(target).use { targetOutputStream ->
                IoUtils.copyAllBytes(inputStream, targetOutputStream)
            }
        }

        return target
    }
}
