package com.elta.android.presentation.features.profile.settings.global.ui.builder

import android.support.annotation.DrawableRes
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsSeparatorItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsSocialItem
import com.elta.android.presentation.utils.createFullName
import com.elta.android.presentation.utils.toString
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class ProfileSettingsItemsBuilder @Inject constructor(
    private val resources: ResourceProvider
) {

    fun buildItems(profile: Profile) = arrayListOf<ListItem>().apply {
        with(profile) {

            add(createHeaderItem(resources.getString(R.string.profile_personal_information)))
            add(createSettingsItem(R.drawable.ic_acc_name,
                createFullName(resources.getString(R.string.profile_full_name_placeholder)),
                ProfileSettingsItem.Type.NAME))
            add(createSettingsItem(R.drawable.ic_settings_gender,
                createGender(this), ProfileSettingsItem.Type.GENDER))

            add(createHeaderItem(resources.getString(R.string.profile_security)))
            add(createSettingsItem(R.drawable.ic_key_pass,
                resources.getString(R.string.profile_change_password), ProfileSettingsItem.Type.PASSWORD))
            add(createSettingsItem(R.drawable.ic_mail,
                this.email?.let { it } ?: "", ProfileSettingsItem.Type.EMAIL))

            add(createHeaderItem(resources.getString(R.string.profile_linked_social_networks)))
            addAll(createSocialItems(this))

            add(ProfileSettingsSeparatorItem)

            add(createHeaderItem(resources.getString(R.string.profile_additional_settings)))
            add(createSettingsItem(R.drawable.ic_notification,
                resources.getString(R.string.profile_notification), ProfileSettingsItem.Type.NOTIFICATION))
            add(createSettingsItem(R.drawable.ic_doc,
                resources.getString(R.string.profile_legal_information), ProfileSettingsItem.Type.LEGAL_INFO))
            add(createSettingsItem(R.drawable.ic_app_info, resources.getString(R.string.profile_app_version,
                BuildConfig.VERSION_NAME), ProfileSettingsItem.Type.APP_VERSION))
        }
    }

    private fun createHeaderItem(title: String) =
        ProfileSettingsHeaderItem(title)

    private fun createSettingsItem(@DrawableRes icon: Int, title: String, type: ProfileSettingsItem.Type) =
        ProfileSettingsItem(icon, title, type)

    private fun createSocialItems(profile: Profile): List<ListItem> {
        val socialNetworks = mutableListOf<ListItem>()
        profile.socialNetworks?.let { list ->
            list.forEach { network ->
                when (network.type) {
                    SocialNetworkType.FB -> socialNetworks.add(createSettingsSocialItem(R.drawable.ic_facebook,
                        resources.getString(R.string.facebook), network.isLinked, SocialNetworkType.FB))
                    SocialNetworkType.VK -> socialNetworks.add(createSettingsSocialItem(R.drawable.ic_vk,
                        resources.getString(R.string.vkontakte), network.isLinked, SocialNetworkType.VK))
                    SocialNetworkType.OK -> socialNetworks.add(createSettingsSocialItem(R.drawable.ic_ok,
                        resources.getString(R.string.odnoklasniki), network.isLinked, SocialNetworkType.OK))
                }
            }
        }
        return socialNetworks
    }

    private fun createSettingsSocialItem(
        @DrawableRes networkIcon: Int,
        title: String,
        isLinked: Boolean,
        type: SocialNetworkType
    ) = ProfileSettingsSocialItem(networkIcon, title, isLinked, type)

    private fun createGender(profile: Profile) =
        profile.gender?.toString(resources)
            ?: resources.getString(R.string.profile_gender_placeholder)
}