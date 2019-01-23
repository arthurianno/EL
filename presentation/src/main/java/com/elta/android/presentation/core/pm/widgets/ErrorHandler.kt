package com.elta.android.presentation.core.pm.widgets

import com.elta.android.common.errors.NetworkConnectionError
import com.elta.android.common.errors.RemoteAuthError
import com.elta.android.presentation.R
import com.elta.android.presentation.States
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.messages.SnackbarMessageData

@Suppress("EmptyWhenBlock")
class ErrorHandler(private val pm: BasePm) {

    fun handleError(error: Throwable) {
        // TODO logic of error processing should be improved
        when (error) {
            is RemoteAuthError -> {
                pm.passToErrorContainer(States.SimpleError(icon = R.drawable.ic_warning, description = error.message))
                pm.passToErrorViewVisibility(true)
            }
            is NetworkConnectionError -> {
            } // TODO logic of Network exception should be improved
            else -> pm.showSnackBar(
                SnackbarMessageData.SimpleTextMessage(pm.resources.getString(R.string.error_test_error))
            )
//            is UnauthorizedException -> pm.router.newRootScreen(Screens.SCREEN_AUTH_LOGOUT, false)
//            else -> {
//                val errorData = when (error) {
//                    is NetworkConnectionError -> ErrorData.InternetErrorData(pm.resources)
//                    is ConnectException -> ErrorData.ServerConnectionErrorData(pm.resources)
//                    is SocketTimeoutException -> ErrorData.ServerConnectionErrorData(pm.resources)
//                    is ServiceUnavailableError -> ErrorData.ServerConnectionErrorData(pm.resources)
//                    is HttpException -> ErrorData.ApiError(pm.errorParser.parse(error))
//                    else -> ErrorData.ServerConnectionErrorData(pm.resources)
//                }
//                pm.passToErrorContainer(errorData)
//                if (pm.isEmptyScreen) {
//                    if (error is NetworkConnectionError ||
//                        error is ConnectException ||
//                        error is SocketTimeoutException ||
//                        error is ServiceUnavailableError
//                    ) {
//                        pm.passToErrorViewVisibility(true)
//                    } else {
//                        pm.showSnackBar(SnackBarData.OtherError(errorData.errorDescription))
//                    }
//                } else {
//                    pm.passToErrorViewVisibility(false)
//                    pm.showSnackBar(SnackBarData.OtherError(errorData.errorDescription))
//                }
//            }
        }
    }
}

fun BasePm.errorHandler() = ErrorHandler(this)