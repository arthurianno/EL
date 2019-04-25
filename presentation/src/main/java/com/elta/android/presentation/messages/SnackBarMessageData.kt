package com.elta.android.presentation.messages

import com.elta.android.presentation.core.ui.snack_bar_view.SnackBarData

sealed class SnackBarMessageData(
    override val icon: Int? = null,
    override val message: String,
    override val button: String? = null
) : SnackBarData {

    class SimpleTextMessage(message: String) : SnackBarMessageData(message = message)

    class WithButton(message: String, button: String): SnackBarMessageData(message = message, button = button)
}