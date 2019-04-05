package com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates

import com.elta.android.presentation.R
import com.nullgr.core.adapter.ktx.AdapterDelegate

class ProfileSettingsSeparatorDelegate : AdapterDelegate() {

    override val itemType = ProfileSettingsSeparatorDelegate::class
    override val layoutResource = R.layout.item_separator
}