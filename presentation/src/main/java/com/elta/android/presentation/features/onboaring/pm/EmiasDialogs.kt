package com.elta.android.presentation.features.onboaring.pm

import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.nullgr.core.resources.ResourceProvider

internal class EmiasDialogs(private val resourceProvider: ResourceProvider) {
    val userConnectedDialogData: DialogData by lazy {
        Dialogs.Emias.UserConnected(resourceProvider)
    }

    val userNotFoundDialogData: DialogData by lazy {
        Dialogs.Emias.UserNotFound(resourceProvider)
    }

    val agreementNotFoundDialogData: DialogData by lazy {
        Dialogs.Emias.AgreementNotFound(resourceProvider)
    }

    val internalErrorDialogData: DialogData by lazy {
        Dialogs.Emias.InternalError(resourceProvider)
    }

    val userAlreadyLinkedDialogData: DialogData by lazy {
        Dialogs.Emias.UserAlreadyLinked(resourceProvider)
    }

    val badInternetConnectionDialogData: DialogData by lazy {
        Dialogs.Emias.BadInternetConnection(resourceProvider)
    }
}
