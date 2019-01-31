package com.elta.android.presentation.core.pm.widgets

import com.elta.android.common.errors.InvalidRefreshTokenError
import com.elta.android.common.errors.ServiceUnavailableError
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.States
import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.messages.SnackBarMessageData
import java.net.ConnectException
import java.net.SocketTimeoutException

@Suppress("EmptyWhenBlock")
class ErrorHandler(private val pm: BasePm) {

    fun handleError(error: Throwable) {
        when (error) {
            is InvalidRefreshTokenError -> {
                val r = pm.router
                if (r is FlowRouter) {
                    r.newRootFlow(Screens.AuthFlow)
                } else {
                    r.newRootScreen(Screens.AuthFlow)
                }
            }
            else ->
                if (pm.isEmptyScreen) {
                    if (error.isServerUnavailableError()) {
                        pm.setErrorStateData(States.ServerError(pm.resources))
                    } else {
                        pm.setErrorStateData(
                            States.SimpleError(
                                icon = R.drawable.ic_server_error,
                                title = pm.resources.getString(R.string.error_general_title),
                                description = error.message,
                                button = pm.resources.getString(R.string.error_general_button)
                            )
                        )
                    }
                    pm.setErrorViewVisibility(true)
                } else {
                    pm.setErrorViewVisibility(false)
                    pm.showSnackBar(SnackBarMessageData.SimpleTextMessage(error.message ?: ""))
                }
        }
    }

    private fun Throwable.isServerUnavailableError(): Boolean =
        this is ConnectException || this is SocketTimeoutException || this is ServiceUnavailableError
}

fun BasePm.errorHandler() = ErrorHandler(this)