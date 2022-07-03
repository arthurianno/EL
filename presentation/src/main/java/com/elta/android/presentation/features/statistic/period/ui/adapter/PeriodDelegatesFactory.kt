package com.elta.android.presentation.features.statistic.period.ui.adapter

import androidx.recyclerview.widget.RecyclerView
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
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class PeriodDelegatesFactory @Inject constructor(
    private val bus: RxBus,
    private val viewPool: RecyclerView.RecycledViewPool
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            GlucoseDailyChartItem::class.java -> GlucoseDailyChartDelegate()
            GlucoseStatisticChartItem::class.java -> GlucoseStatisticChartDelegate(bus)
            GlucoseIndexItem::class.java -> GlucoseIndexDelegate()
            GlucoseIndexesItem::class.java -> GlucoseIndexesDelegate(viewPool, this)
            GeneralIndexItem::class.java -> GeneralIndexDelegate()
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}
