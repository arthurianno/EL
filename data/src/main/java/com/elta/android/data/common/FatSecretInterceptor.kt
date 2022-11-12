package com.elta.android.data.common

import com.elta.android.data.features.calculator.storage.FatSecretStorage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

private const val ACCEPT_ENCODING = "Accept-Encoding"
private const val ACCEPT_ENCODING_VALUE = "identity"

@Singleton
class FatSecretInterceptor @Inject constructor(
    private val storage: FatSecretStorage
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(
            chain.request()
                .newBuilder()
                .addHeader(AUTH_HEADER, "$PREFIX ${storage.token}")
                .addHeader(ACCEPT_ENCODING, ACCEPT_ENCODING_VALUE)
                .build()
        )
}
