package com.elta.android.data.common

import com.elta.android.data.features.auth.storage.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenInterceptor @Inject constructor(
    private val storage: TokenStorage
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        storage.accessToken?.let { token -> builder.addHeader(AUTH_HEADER, token) }

        val request = builder.method(original.method(), original.body()).build()
        return chain.proceed(request)
    }

    companion object {
        private const val AUTH_HEADER = "Authorization"
    }
}