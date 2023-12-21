package com.elta.android.presentation.features.profile.settings.global.ui.adapter.items

import com.elta.android.domain.features.appsettings.model.BackendVariant
import com.nullgr.core.adapter.items.ListItem

data class ProfileSettingRadioButtonItem(
    val type: BackendVariant
) : ListItem {
    override fun getUniqueProperty() = type
}
