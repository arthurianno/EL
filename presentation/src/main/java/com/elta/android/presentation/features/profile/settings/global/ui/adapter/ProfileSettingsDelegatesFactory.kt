package com.elta.android.presentation.features.profile.settings.global.ui.adapter

import com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates.ProfileSettingsHeaderDelegate
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class ProfileSettingsDelegatesFactory @Inject constructor(
    private val bus: RxBus
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            ProfileSettingsHeaderItem::class.java -> ProfileSettingsHeaderDelegate()
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}