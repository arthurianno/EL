package com.elta.android.presentation.features.profile.support.ui.builder

import com.elta.android.domain.features.FeatureToggles
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.support.model.SupportAction
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportActionItem
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportHeaderItem
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportVersionItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class SupportItemsBuilder @Inject constructor(
    private val resourceProvider: ResourceProvider
) {
    fun buildItems(glucometerVersion: String): List<ListItem> = mutableListOf(
        SupportHeaderItem(text = resourceProvider.getString(R.string.profile_support_actions_header)),
        SupportActionItem(
            icon = R.drawable.ic_support_call,
            title = resourceProvider.getString(R.string.profile_support_phone_number),
            subTitle = resourceProvider.getString(R.string.profile_support_phone_number_description),
            action = SupportAction.CallAction(resourceProvider.getString(R.string.profile_support_phone_number))
        ),
        SupportActionItem(
            icon = R.drawable.ic_support_mail,
            title = resourceProvider.getString(R.string.profile_support_email),
            subTitle = resourceProvider.getString(R.string.profile_support_email_description),
            action = SupportAction.MailAction(resourceProvider.getString(R.string.profile_support_email))
        ),
        SupportActionItem(
            icon = R.drawable.ic_telegram,
            title = resourceProvider.getString(R.string.telegram),
            subTitle = resourceProvider.getString(R.string.profile_support_email_description),
            action = SupportAction.TelegramAction
        ),
        SupportActionItem(
            icon = R.drawable.ic_whatsapp,
            title = resourceProvider.getString(R.string.whatsapp),
            subTitle = resourceProvider.getString(R.string.profile_support_email_description),
            action = SupportAction.WhatsAppAction
        ),
        SupportActionItem(
            icon = R.drawable.ic_viber,
            title = resourceProvider.getString(R.string.viber),
            subTitle = resourceProvider.getString(R.string.profile_support_email_description),
            action = SupportAction.ViberAction
        ),
        SupportActionItem(
            icon = R.drawable.ic_support_center,
            title = resourceProvider.getString(R.string.profile_support_service_centers),
            subTitle = resourceProvider.getString(R.string.profile_support_service_centers_description),
            action = SupportAction.ServiceCentersAction
        ),
        SupportHeaderItem(
            text = resourceProvider.getString(R.string.profile_support_versions_header)
        ),
        SupportVersionItem(
            title = resourceProvider.getString(R.string.profile_support_firmware_version),
            version = glucometerVersion
        ),
        SupportVersionItem(
            title = resourceProvider.getString(R.string.profile_support_app_version),
            version = BuildConfig.APP_VERSION
        )
    ).apply {
        if (FeatureToggles.isEnableConsultantFeature) {
            add(
                index = 2,
                element = SupportActionItem(
                    icon = R.drawable.ic_chat,
                    title = resourceProvider.getString(R.string.profile_support_consultant),
                    subTitle = resourceProvider.getString(R.string.profile_support_email_description),
                    action = SupportAction.ConsultantAction
                )
            )
        }
    }
}
