package com.elta.android.presentation.features.profile.settings.ui.adapter

import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class ProfileSettingsDelegatesFactory @Inject constructor(
    private val bus: RxBus
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
}