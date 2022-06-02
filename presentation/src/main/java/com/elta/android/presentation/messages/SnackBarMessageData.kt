package com.elta.android.presentation.messages

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.snack_bar_view.SnackBarData
import com.nullgr.core.resources.ResourceProvider

sealed class SnackBarMessageData(
    override val icon: Int? = null,
    override val message: String,
    override val button: String? = null,
    override val duration: Int? = null
) : SnackBarData {

    class SimpleTextMessage(message: String) : SnackBarMessageData(message = message)

    class WithButton(
        message: String,
        button: String,
        duration: Int? = null
    ) : SnackBarMessageData(message = message, button = button, duration = duration)

    class ServerUnavailableMessage(resourceProvider: ResourceProvider) :
        SnackBarMessageData(
            message = resourceProvider.getString(R.string.error_server_not_responding_snackbar_message)
        )
}
