package com.elta.android.domain.common.repository

import com.elta.android.domain.common.model.FileDirectory
import java.io.File

interface DownloadRepository {

    suspend fun downloadFile(
        url: String,
        destinationDirectory: FileDirectory,
        destinationFile: String
    ): File
}
