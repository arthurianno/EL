package com.elta.android.data.common

import com.elta.android.common.errors.FatSecretErrors
import com.elta.android.common.errors.ServerError
import com.elta.android.common.errors.ServiceUnavailableError
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.GzipSource
import okio.buffer
import java.io.InputStreamReader
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton

private const val ACCEPT_ENCODING = "Accept-Encoding"
private const val ACCEPT_ENCODING_VALUE = "gzip"
private const val ERROR_BODY_LENGTH = 2048L
private const val ERROR_CODE_500 = 500
private const val ERROR_INVALID_TOKEN = 13

@Singleton
class FatSecretErrorInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val error = getErrorBody(request, response.peekBody(ERROR_BODY_LENGTH))
        handleError(response, error)
        return response
    }

    private fun getErrorBody(
        request: Request,
        body: ResponseBody
    ) = runCatching {
        val streamReader = GzipSource(body.source()).buffer().inputStream()
        if (request.header(ACCEPT_ENCODING)?.contains(ACCEPT_ENCODING_VALUE) == true) {
            val parseReader = JsonParser.parseReader(InputStreamReader(streamReader))
            Gson().fromJson(
                parseReader,
                FatSecretErrorBody::class.java
            )
        } else {
            Gson().fromJson(body.string(), FatSecretErrorBody::class.java)
        }
    }.getOrNull()

    private fun handleError(
        response: Response,
        error: FatSecretErrorBody?
    ): Nothing? =
        when (response.code) {
            HttpURLConnection.HTTP_BAD_REQUEST -> throw ServerError(message = response.message)
            ERROR_CODE_500 -> throw ServiceUnavailableError(message = response.message)
            else -> error?.error?.run {
                when (code) {
                    ERROR_INVALID_TOKEN -> throw FatSecretErrors.TokenError(message)
                    else -> throw ServerError(message)
                }
            }
        }

    private data class FatSecretErrorBody(
        @SerializedName("error") val error: Error
    ) {
        data class Error(
            @SerializedName("code") val code: Int,
            @SerializedName("message") val message: String
        )
    }
}
