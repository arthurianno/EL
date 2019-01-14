package com.nullgr.android.presentation.core.pm.widgets

import com.nullgr.android.presentation.core.pm.BasePm

class ErrorHandler(private val pm: BasePm) {

    fun handleError(error: Throwable) {
        when (error) {
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