package com.elta.android.presentation.features.statistic.period.ui.adapter

import android.support.v7.widget.RecyclerView
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates.ProfileSettingsHeaderDelegate
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.delegates.GeneralIndexDelegate
import com.elta.android.presentation.features.statistic.period.ui.adapter.delegates.GlucoseDailyChartDelegate
import com.elta.android.presentation.features.statistic.period.ui.adapter.delegates.GlucoseIndexDelegate
import com.elta.android.presentation.features.statistic.period.ui.adapter.delegates.GlucoseIndexesDelegate
import com.elta.android.presentation.features.statistic.period.ui.adapter.delegates.GlucoseStatisticChartDelegate
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GeneralIndexItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseDailyChartItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexesItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseStatisticChartItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class PeriodDelegatesFactory @Inject constructor(
    private val bus: RxBus,
    private val viewPool: RecyclerView.RecycledViewPool,
    private val calculator: DiffCalculator
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            GlucoseDailyChartItem::class.java -> GlucoseDailyChartDelegate()
            GlucoseStatisticChartItem::class.java -> GlucoseStatisticChartDelegate(bus)
            GlucoseIndexItem::class.java -> GlucoseIndexDelegate()
            GlucoseIndexesItem::class.java -> GlucoseIndexesDelegate(this, calculator, viewPool)
            GeneralIndexItem::class.java -> GeneralIndexDelegate()
            ProfileSettingsHeaderItem::class.java -> ProfileSettingsHeaderDelegate()
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}