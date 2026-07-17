package com.elta.android.data.common

import android.content.Context
import com.elta.android.common.errors.CantSendInviteToYourselfError
import com.elta.android.common.errors.EmailAlreadyConfirmedError
import com.elta.android.common.errors.EmailAlreadyInvitedError
import com.elta.android.common.errors.EmailAlreadyRegisteredError
import com.elta.android.common.errors.EmailLinkInvalid
import com.elta.android.common.errors.EmiasError
import com.elta.android.common.errors.IncorrectLoginOrPasswordError
import com.elta.android.common.errors.InvalidRefreshTokenError
import com.elta.android.common.errors.NotFoundError
import com.elta.android.common.errors.ProfileIsDeletedError
import com.elta.android.common.errors.ServerError
import com.elta.android.common.errors.ServiceUnavailableError
import com.elta.android.common.errors.SocialNetworkAlreadyRegisteredError
import com.elta.android.common.errors.UnauthorizedError
import com.elta.android.data.common.model.ErrorBody
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

private const val ERROR_CODE_400 = 400
private const val ERROR_CODE_403 = 403
private const val ERROR_CODE_404 = 404
private const val ERROR_CODE_410 = 410
private const val ERROR_CODE_600 = 600
private const val ERROR_CODE_603 = 603
private const val ERROR_CODE_604 = 604
private const val ERROR_CODE_605 = 605
private const val ERROR_CODE_606 = 606
private const val ERROR_CODE_607 = 607
private const val ERROR_CODE_610 = 610
private const val ERROR_CODE_700 = 700
private const val ERROR_CODE_707 = 707
private const val SERVER_ERROR = 5
private const val SERVER_ERROR_502 = 502

@Singleton
class ErrorInterceptor @Inject constructor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val responseCode = response.code
        when {
            responseCode == ERROR_CODE_400 -> {
                val errorBody = getErrorBody(response.body)
                when (errorBody?.errorCode) {
                    EmiasError.OMS_ALREADY_LINKED -> throw EmiasError.OmsAlreadyLinked
                    EmiasError.USER_IN_EMIAS_NOT_FOUND -> throw EmiasError.UserInEmiasNotFound
                    EmiasError.AGREEMENT_FOR_EMIAS_USAGE_NOT_FOUND -> throw EmiasError.AgreementForEmiasUsageNotFound
                    else -> throw ServiceUnavailableError("response code = $responseCode")
                }
            }

            responseCode == ERROR_CODE_404 -> throw NotFoundError(response.message)
            responseCode == ERROR_CODE_403 -> throw UnauthorizedError()
            responseCode == ERROR_CODE_410 -> throw ProfileIsDeletedError(response.message)

            responseCode.firstDigit() == SERVER_ERROR -> {
                when (responseCode) {
                    SERVER_ERROR_502 -> {
                        val errorBody = getErrorBody(response.body)
                        when (errorBody?.errorCode) {
                            EmiasError.EMIAS_INTERNAL_ERROR -> throw EmiasError.EmiasInternalError
                            else -> throw ServiceUnavailableError("response code = $responseCode")
                        }
                    }
                    else -> throw ServiceUnavailableError("response code = $responseCode")
                }
            }

            responseCode >= ERROR_CODE_600 -> {
                val message = getStringByCode(responseCode)
                when (responseCode) {
                    ERROR_CODE_600 -> throw IncorrectLoginOrPasswordError(message)
                    ERROR_CODE_603 -> throw EmailAlreadyRegisteredError(message)
                    ERROR_CODE_604 -> throw InvalidRefreshTokenError(message)
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

    private fun getErrorBody(body: ResponseBody?): ErrorBody? = runCatching {
        body?.let { Gson().fromJson(it.string(), ErrorBody::class.java) }
    }.getOrNull()

    private fun getStringByCode(code: Int): String {
        val res = context.resources.getIdentifier("error_$code", "string", context.packageName)
        return if (res != 0) context.getString(res) else "$code"
    }
}

internal fun Int.firstDigit() = this / 100
