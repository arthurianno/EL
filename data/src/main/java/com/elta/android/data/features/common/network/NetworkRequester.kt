package com.elta.android.data.features.common.network

import okhttp3.ResponseBody

interface NetworkRequester {
    suspend fun request(url: String): ResponseBody
}
