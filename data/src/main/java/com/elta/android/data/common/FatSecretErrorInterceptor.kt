package com.elta.android.data.common

import com.elta.android.common.errors.FatSecretErrors
import com.elta.android.common.errors.ServerError
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton

private const val ERROR_INVALID_TOKEN = 13
private const val ERROR_MISSING_SCOPE = 14

@Singleton
class FatSecretErrorInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val body = response.peekBody(2048).string()
        val code = response.code()
        val message = response.message()
        val error = runCatching {
            Gson().fromJson(body, FatSecretErrorBody::class.java)
        }
            .getOrNull()
        when (code) {
            HttpURLConnection.HTTP_BAD_REQUEST -> throw ServerError(message = message)
            else -> error?.error?.run {
                when (this.code) {
                    ERROR_INVALID_TOKEN -> throw FatSecretErrors.TokenError(this.message)
                    else -> {
                        return response
                    }
                }
            }
        }
        return response
    }

    data class FatSecretErrorBody(
        @SerializedName("error") val error: Error
    ) {
        data class Error(
            @SerializedName("code") val code: Int,
            @SerializedName("message") val message: String
        )
    }
}
