package com.elta.android.data.common.datasource.download

import java.io.InputStream

interface DownloadSource {

    suspend fun download(url: String): InputStream
}
