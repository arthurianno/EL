package com.elta.android.presentation.features.registration.main.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import com.elta.android.presentation.features.registration.main.model.RegistrationAction

internal fun registrationConsentText(
    description: String,
    privacyPolicyLabel: String,
    personalDataLabel: String,
    linkStyles: TextLinkStyles,
    onAction: (RegistrationAction) -> Unit
): AnnotatedString {
    // Resource line breaks must not split a link or force a fixed phone-width layout.
    val text = description.replace(Regex("\\s+"), " ").trim()
    return buildAnnotatedString {
        append(text)
        fun addDocumentLink(label: String, tag: String, action: RegistrationAction) {
            val normalizedLabel = label.replace(Regex("\\s+"), " ").trim()
            val start = text.indexOf(normalizedLabel)
            if (start >= 0 && normalizedLabel.isNotEmpty()) {
                addLink(
                    LinkAnnotation.Clickable(tag, linkStyles) { onAction(action) },
                    start,
                    start + normalizedLabel.length
                )
            }
        }
        addDocumentLink(privacyPolicyLabel, "privacy_policy", RegistrationAction.PrivacyPolicyClicked)
        addDocumentLink(personalDataLabel, "personal_data", RegistrationAction.PersonalDataClicked)
    }
}
