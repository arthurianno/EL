package com.elta.android.presentation.features.profile.support.model

sealed class SupportAction {
    data class CallAction(val phone: String) : SupportAction()
    data class MailAction(val email: String) : SupportAction()
    object ServiceCentersAction : SupportAction()
}