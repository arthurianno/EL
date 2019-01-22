package com.elta.android.data.common

import android.content.Context
import com.elta.android.common.errors.EmailAlreadyRegisteredError
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("MagicNumber")
@Singleton
class ErrorInterceptor @Inject constructor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response? {
        val request = chain.request()
        val response = chain.proceed(request)

        val responseCode = response.code()
        when {
            responseCode > ERROR_CODE_600 -> {
                val res = getStringByCode(context, responseCode)
                val message = context.getString(res)
                when (responseCode) {
                    603 -> throw EmailAlreadyRegisteredError(message)
                }
            }
        }
        return response
    }

    companion object {
        const val ERROR_CODE_400 = 400
        const val ERROR_CODE_600 = 600

        fun getStringByCode(context: Context, code: Int): Int =
            context.resources.getIdentifier("error_$code", "string", context.packageName)
    }
}