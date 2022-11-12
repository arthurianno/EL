package com.elta.android.data.common

import com.elta.android.data.features.auth.storage.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

internal const val PREFIX = "Bearer"
internal const val AUTH_HEADER = "Authorization"

@Singleton
class TokenInterceptor @Inject constructor(
    private val storage: TokenStorage
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()

        if (chain.request().header(AUTH_HEADER) == null) {
            storage.accessToken?.let { token ->
                builder.addHeader(AUTH_HEADER, "$PREFIX $token")
            }
        }

        return chain.proceed(builder.build())
    }
}
