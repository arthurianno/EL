package com.elta.android.data.core.network

import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.concurrent.Executor

object RetrofitFactory {

    @Suppress("LongParameterList", "LongMethod")
    fun create(
        okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory,
        converterFactory: Converter.Factory,
        url: String,
        executor: Executor? = null
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addCallAdapterFactory(callAdapterFactory)
            .addConverterFactory(converterFactory)
            .also { builder ->
                if (executor != null) {
                    builder.callbackExecutor(executor)
                }
            }
            .build()
    }
}