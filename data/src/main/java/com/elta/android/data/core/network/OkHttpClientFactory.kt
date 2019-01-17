package com.elta.android.data.core.network

import com.elta.android.data.core.qualifires.Interceptors
import com.elta.android.data.core.qualifires.NetworkInterceptors
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

object OkHttpClientFactory {
    private const val DEFAULT_TIME_OUT = 30L

    private const val CONNECTION_TIMEOUT: Long = DEFAULT_TIME_OUT
    private const val READ_TIMEOUT: Long = DEFAULT_TIME_OUT
    private const val WRITE_TIMEOUT: Long = DEFAULT_TIME_OUT

    private const val CACHE_SIZE: Long = 1024 * 1024 * 10 // 10Mb

    fun create(
        cacheFolder: File,
        @Interceptors interceptors: List<Interceptor>,
        @NetworkInterceptors networkInterceptors: List<Interceptor>
    ): OkHttpClient =
        OkHttpClient().newBuilder()
            .apply {
                cache(Cache(cacheFolder, CACHE_SIZE))

                for (interceptor in interceptors) {
                    addInterceptor(interceptor)
                }

                for (interceptor in networkInterceptors) {
                    addNetworkInterceptor(interceptor)
                }

                connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)

            }
            .build()
}