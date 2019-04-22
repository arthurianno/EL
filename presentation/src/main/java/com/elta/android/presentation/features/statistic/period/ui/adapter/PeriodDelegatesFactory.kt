package com.elta.android.presentation.features.statistic.period.ui.adapter

import com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates.ProfileSettingsHeaderDelegate
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.delegates.GlucoseIndexDelegate
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import javax.inject.Inject

class PeriodDelegatesFactory @Inject constructor() : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            GlucoseIndexItem::class.java -> GlucoseIndexDelegate()
            ProfileSettingsHeaderItem::class.java -> ProfileSettingsHeaderDelegate()
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}