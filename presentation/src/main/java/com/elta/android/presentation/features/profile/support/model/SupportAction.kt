package com.elta.android.presentation.features.profile.support.model

sealed class SupportAction {
    object ConsultantAction : SupportAction()
    data class CallAction(val phone: String) : SupportAction()
    data class MailAction(val email: String) : SupportAction()
    object TelegramAction : SupportAction()
    object WhatsAppAction : SupportAction()
    object ViberAction : SupportAction()
    object ServiceCentersAction : SupportAction()
    object AppVersionAction : SupportAction()
}
