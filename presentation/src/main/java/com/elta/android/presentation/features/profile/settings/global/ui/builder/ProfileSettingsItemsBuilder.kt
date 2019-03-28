package com.elta.android.presentation.features.profile.settings.global.ui.builder

import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class ProfileSettingsItemsBuilder @Inject constructor(
    private val resources: ResourceProvider
) {

    fun buildItems() = arrayListOf<ListItem>().apply {
        add(createHeaderItem(resources.getString(R.string.profile_personal_information)))
        add(createHeaderItem(resources.getString(R.string.profile_security)))
        add(createHeaderItem(resources.getString(R.string.profile_linked_social_networks)))
        add(createHeaderItem(resources.getString(R.string.profile_additional_settings)))
    }

    private fun createHeaderItem(title: String) =
        ProfileSettingsHeaderItem(title)
}