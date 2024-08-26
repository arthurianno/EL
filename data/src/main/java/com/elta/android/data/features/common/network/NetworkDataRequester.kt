package com.elta.android.data.features.common.network

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class NetworkDataRequester @Inject constructor(
    private val httpLoggingInterceptor: HttpLoggingInterceptor
) : NetworkRequester {
    override suspend fun request(url: String): ResponseBody {
        return suspendCoroutine { continuation ->
            val client = OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build()
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response.body)
                }
            })
        }
    }
}
