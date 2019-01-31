package com.elta.android.presentation.features.onboaring.ui.adapter

import com.elta.android.presentation.features.onboaring.ui.adapter.delegates.OnBoardingGenderDelegate
import com.elta.android.presentation.features.onboaring.ui.adapter.delegates.TestDelegate
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingGenderItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.TestItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class OnBoardingDelegatesFactory @Inject constructor(
    private val bus: RxBus
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            TestItem::class.java -> TestDelegate()
            OnBoardingGenderItem::class.java -> OnBoardingGenderDelegate(bus)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}