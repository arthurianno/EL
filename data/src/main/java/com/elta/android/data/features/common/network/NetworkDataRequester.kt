package com.elta.android.data.features.common.network

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class NetworkDataRequester @Inject constructor(
    private val okHttpClient: OkHttpClient
) : NetworkRequester {
    override suspend fun request(url: String): ResponseBody {
        return suspendCoroutine { continuation ->
            val request = Request.Builder()
                .url(url)
                .build()

            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val body = response.body
                        if (body != null) {
                            continuation.resume(body)
                        } else {
                            continuation.resumeWithException(IOException("Response body is null"))
                        }
                    } else {
                        continuation.resumeWithException(IOException("Server returned error code: ${response.code}"))
                    }
                }
            })
        }
    }
}
