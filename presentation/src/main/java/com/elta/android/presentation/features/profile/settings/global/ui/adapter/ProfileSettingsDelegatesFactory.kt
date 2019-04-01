package com.elta.android.presentation.features.profile.settings.global.ui.adapter

import com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates.ProfileSettingsDelegate
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates.ProfileSettingsHeaderDelegate
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates.ProfileSettingsSeparatorDelegate
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates.ProfileSettingsSocialDelegate
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsSeparatorItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsSocialItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class ProfileSettingsDelegatesFactory @Inject constructor(
    private val bus: RxBus,
    private val resources: ResourceProvider
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            ProfileSettingsHeaderItem::class.java -> ProfileSettingsHeaderDelegate()
            ProfileSettingsItem::class.java -> ProfileSettingsDelegate(bus, resources)
            ProfileSettingsSocialItem::class.java -> ProfileSettingsSocialDelegate(bus)
            ProfileSettingsSeparatorItem::class.java -> ProfileSettingsSeparatorDelegate()
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}