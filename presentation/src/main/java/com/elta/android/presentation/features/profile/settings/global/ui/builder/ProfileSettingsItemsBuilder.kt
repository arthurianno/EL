package com.elta.android.presentation.features.profile.settings.global.ui.builder

import android.support.annotation.DrawableRes
import com.elta.android.domain.features.auth.model.SocialNetwork
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsSocialItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class ProfileSettingsItemsBuilder @Inject constructor(
    private val resources: ResourceProvider
) {

    fun buildItems() = arrayListOf<ListItem>().apply {
        add(createHeaderItem(resources.getString(R.string.profile_personal_information)))
        add(createSettingsItem(R.drawable.ic_acc_name,
            resources.getString(R.string.profile_full_name_placeholder), ProfileSettingsItem.Type.NAME))
        add(createSettingsItem(R.drawable.ic_settings_gender,
            resources.getString(R.string.profile_gender_placeholder), ProfileSettingsItem.Type.GENDER))

        add(createHeaderItem(resources.getString(R.string.profile_security)))
        add(createSettingsItem(R.drawable.ic_key_pass,
            resources.getString(R.string.profile_change_password), ProfileSettingsItem.Type.PASSWORD))
        add(createSettingsItem(R.drawable.ic_mail,
            "testmail@mail.com", ProfileSettingsItem.Type.EMAIL))

        add(createHeaderItem(resources.getString(R.string.profile_linked_social_networks)))
        add(createSettingsSocialItem(R.drawable.ic_facebook, resources.getString(R.string.facebook),
            R.drawable.ic_delete, SocialNetwork.FB))
        add(createSettingsSocialItem(R.drawable.ic_vk, resources.getString(R.string.vkontakte),
            R.drawable.ic_add, SocialNetwork.VK))
        add(createSettingsSocialItem(R.drawable.ic_ok, resources.getString(R.string.odnoklasniki),
            R.drawable.ic_add, SocialNetwork.OK))

        add(createHeaderItem(resources.getString(R.string.profile_additional_settings)))
        add(createSettingsItem(R.drawable.ic_notification,
            resources.getString(R.string.profile_notification), ProfileSettingsItem.Type.NOTIFICATION))
        add(createSettingsItem(R.drawable.ic_doc,
            resources.getString(R.string.profile_legal_information), ProfileSettingsItem.Type.LEGAL_INFO))
        add(createSettingsItem(R.drawable.ic_app_info, resources.getString(R.string.profile_app_version,
            "2.02.343"), ProfileSettingsItem.Type.APP_VERSION))
    }

    private fun createHeaderItem(title: String) =
        ProfileSettingsHeaderItem(title)

    private fun createSettingsItem(@DrawableRes icon: Int, title: String, type: ProfileSettingsItem.Type) =
        ProfileSettingsItem(icon, title, type)

    private fun createSettingsSocialItem(
        @DrawableRes networkIcon: Int,
        title: String,
        @DrawableRes actionIcon: Int,
        type: SocialNetwork
    ) = ProfileSettingsSocialItem(networkIcon, title, actionIcon, type)
}