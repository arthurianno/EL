package com.elta.android.presentation.features.onboaring.ui.adapter

import com.elta.android.presentation.features.onboaring.ui.adapter.delegates.OnBoardingDiabetesDelegate
import com.elta.android.presentation.features.onboaring.ui.adapter.delegates.OnBoardingGenderDelegate
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingDiabetesItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingGenderItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class OnBoardingDelegatesFactory @Inject constructor(
    private val bus: RxBus,
    private val resources: ResourceProvider
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            OnBoardingGenderItem::class.java -> OnBoardingGenderDelegate(bus)
            OnBoardingDiabetesItem::class.java -> OnBoardingDiabetesDelegate(bus, resources)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}