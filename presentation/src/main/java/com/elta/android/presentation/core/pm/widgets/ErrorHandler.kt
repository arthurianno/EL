package com.elta.android.presentation.core.pm.widgets

import com.elta.android.common.errors.InvalidRefreshTokenError
import com.elta.android.common.errors.ServiceUnavailableError
import com.elta.android.common.errors.SocialAuthError
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.States
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.messages.SnackBarMessageData
import java.net.ConnectException
import java.net.SocketTimeoutException

@Suppress("EmptyWhenBlock", "LongMethod")
class ErrorHandler(private val pm: BasePm) {

    fun handleError(error: Throwable) {
        when (error) {
            is InvalidRefreshTokenError -> pm.router.newRootFlow(Screens.AuthFlow)
            is SocialAuthError -> handleSocialAuthError(error)
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

    private fun handleSocialAuthError(error: SocialAuthError) {
        // temporary ignored
    }
}

fun BasePm.errorHandler() = ErrorHandler(this)