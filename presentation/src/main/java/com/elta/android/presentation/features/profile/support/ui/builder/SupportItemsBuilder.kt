package com.elta.android.presentation.features.profile.support.ui.builder

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
    private var showExtraVersions = false

    fun buildItems(glucometerVersion: String): List<ListItem> {
        val items = mutableListOf<ListItem>(
            SupportHeaderItem(text = resourceProvider.getString(R.string.profile_support_actions_header)),
            /*SupportActionItem(
                icon = R.drawable.ic_chat,
                title = resourceProvider.getString(R.string.profile_support_consultant),
                subTitle = resourceProvider.getString(R.string.profile_support_email_description),
                action = SupportAction.ConsultantAction
            ),*/
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
            SupportHeaderItem(
                text = resourceProvider.getString(R.string.profile_support_versions_header)
            ),
            SupportVersionItem(
                title = resourceProvider.getString(R.string.profile_support_firmware_version),
                version = glucometerVersion
            ),
            SupportVersionItem(
                title = resourceProvider.getString(R.string.profile_support_app_version),
                version = BuildConfig.APP_VERSION + " (${BuildConfig.BUILD_TYPE})",
                action = SupportAction.AppVersionAction // Делаем элемент кликабельным
            )
        )
        if (showExtraVersions) {
            items.addAll(listOf(
                SupportVersionItem(
                    title = resourceProvider.getString(R.string.profile_support_build_version),
                    version = BuildConfig.BUILD_NUMBER
                ),
                SupportVersionItem(
                    title = resourceProvider.getString(R.string.profile_support_store_version),
                    version = BuildConfig.APP_STORE
                ),
                SupportVersionItem(
                    title = resourceProvider.getString(R.string.profile_support_hotfix_version),
                    version = BuildConfig.HOTFIX_VERSION
                ),
                SupportVersionItem(
                    title = resourceProvider.getString(R.string.profile_support_server),
                    version = BuildConfig.SERVER_URL
                )
            ))
        }
        return items
    }

    fun toggleExtraVersions() {
        showExtraVersions = !showExtraVersions
    }
}