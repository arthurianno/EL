package com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates

import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.ItemSeparatorBinding
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsSeparatorItem
import com.nullgr.core.adapter.ktx.AdapterDelegate

class ProfileSettingsSeparatorDelegate :
    AdapterDelegate<ItemSeparatorBinding>(ItemSeparatorBinding::inflate) {

    override val itemType = ProfileSettingsSeparatorItem::class
    override val layoutResource = R.layout.item_separator
}
