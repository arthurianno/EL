package com.elta.android.data.common.datasource.download

import com.elta.android.data.features.common.network.NetworkRequester
import java.io.InputStream
import javax.inject.Inject

class DownloadDataSource @Inject constructor(
    private val requester: NetworkRequester
) : DownloadSource {
    override suspend fun download(url: String): InputStream =
        requester.request(url).byteStream()
}
