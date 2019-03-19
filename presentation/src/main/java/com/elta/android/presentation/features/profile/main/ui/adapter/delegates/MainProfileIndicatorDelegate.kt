package com.elta.android.presentation.features.profile.main.ui.adapter.delegates

import com.elta.android.presentation.R
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.rx.RxBus

class MainProfileIndicatorDelegate(
    private val bus: RxBus
) : AdapterDelegate() {

    override val itemType = MainProfileIndicatorDelegate::class
    override val layoutResource = R.layout.item_profile_indicators
}