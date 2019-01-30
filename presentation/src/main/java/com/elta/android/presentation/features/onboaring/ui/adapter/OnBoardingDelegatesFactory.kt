package com.elta.android.presentation.features.onboaring.ui.adapter

import com.elta.android.presentation.features.onboaring.ui.adapter.delegates.TestDelegate
import com.elta.android.presentation.features.onboaring.ui.adapter.items.TestItem
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import javax.inject.Inject

class OnBoardingDelegatesFactory @Inject constructor() : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            TestItem::class.java -> TestDelegate()
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}