package com.elta.android.data.common

import com.elta.android.data.features.calculator.storage.FatSecretStorage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FatSecretInterceptor @Inject constructor(
    private val storage: FatSecretStorage
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(
            chain.request()
                .newBuilder()
                .addHeader(AUTH_HEADER, "$PREFIX ${storage.token}")
                .build()
        )
}
