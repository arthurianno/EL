package com.elta.android.data.common

import android.content.Context
import com.elta.android.common.errors.CantSendInviteToYourselfError
import com.elta.android.common.errors.EmailAlreadyConfirmedError
import com.elta.android.common.errors.EmailAlreadyInvitedError
import com.elta.android.common.errors.EmailAlreadyRegisteredError
import com.elta.android.common.errors.EmailLinkInvalid
import com.elta.android.common.errors.IncorrectLoginOrPasswordError
import com.elta.android.common.errors.InvalidRefreshTokenError
import com.elta.android.common.errors.ServerError
import com.elta.android.common.errors.ServiceUnavailableError
import com.elta.android.common.errors.SocialNetworkAlreadyRegisteredError
import com.elta.android.common.errors.UnauthorizedError
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
            responseCode == ERROR_CODE_400 || responseCode.firstDigit() == SERVER_ERROR ->
                throw ServiceUnavailableError()
            responseCode == ERROR_CODE_403 -> throw UnauthorizedError()
            responseCode >= ERROR_CODE_600 -> {
                val message = getStringByCode(context, responseCode)
                when (responseCode) {
                    ERROR_CODE_600 -> throw IncorrectLoginOrPasswordError(message)
                    ERROR_CODE_603 -> throw EmailAlreadyRegisteredError(message)
                    ERROR_CODE_605 -> throw InvalidRefreshTokenError(message)
                    ERROR_CODE_606 -> throw EmailAlreadyConfirmedError(message)
                    ERROR_CODE_607 -> throw EmailLinkInvalid(message)
                    ERROR_CODE_610 -> throw SocialNetworkAlreadyRegisteredError(message)
                    ERROR_CODE_700 -> throw EmailAlreadyInvitedError(message)
                    ERROR_CODE_707 -> throw CantSendInviteToYourselfError(message)
                    else -> throw ServerError(message)
                }
            }
        }
        return response
    }

    private fun Int.firstDigit() = this / 100

    companion object {
        const val ERROR_CODE_400 = 400
        const val ERROR_CODE_403 = 403
        const val ERROR_CODE_600 = 600
        const val ERROR_CODE_603 = 603
        const val ERROR_CODE_605 = 605
        const val ERROR_CODE_606 = 606
        const val ERROR_CODE_607 = 607
        const val ERROR_CODE_610 = 610
        const val ERROR_CODE_700 = 700
        const val ERROR_CODE_707 = 707
        const val SERVER_ERROR = 5

        fun getStringByCode(context: Context, code: Int): String {
            val res = context.resources.getIdentifier("error_$code", "string", context.packageName)
            return if (res != 0) context.getString(res) else "$code"
        }
    }
}