package com.elta.android.presentation.features.statistic.period.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemGlucoseDailyChartBinding
import com.elta.android.presentation.databinding.ItemGlucoseStatisticChartBinding
import com.elta.android.presentation.databinding.ItemStatGeneralIndexBinding
import com.elta.android.presentation.databinding.ItemStatGlucoseIndexBinding
import com.elta.android.presentation.databinding.ItemStatGlucoseIndexesSliderBinding
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GeneralIndexItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseDailyChartItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexesItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseStatisticChartItem
import com.elta.android.presentation.features.statistic.period.ui.holder.GeneralIndexViewHolder
import com.elta.android.presentation.features.statistic.period.ui.holder.GlucoseDailyChartViewHolder
import com.elta.android.presentation.features.statistic.period.ui.holder.GlucoseIndexViewHolder
import com.elta.android.presentation.features.statistic.period.ui.holder.GlucoseIndexesViewHolder
import com.elta.android.presentation.features.statistic.period.ui.holder.GlucoseStatisticChartViewHolder
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class PeriodAdapter @Inject constructor(
    private val bus: RxBus,
    private val viewPool: RecyclerView.RecycledViewPool
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            GlucoseDailyChartItem::class.java.hashCode() -> {
                GlucoseDailyChartViewHolder(
                    ItemGlucoseDailyChartBinding.inflate(inflater, parent, false)
                )
            }
            GlucoseStatisticChartItem::class.java.hashCode() -> {
                GlucoseStatisticChartViewHolder(
                    ItemGlucoseStatisticChartBinding.inflate(inflater, parent, false),
                    bus
                )
            }
            GlucoseIndexItem::class.java.hashCode() -> {
                GlucoseIndexViewHolder(
                    ItemStatGlucoseIndexBinding.inflate(inflater, parent, false)
                )
            }
            GlucoseIndexesItem::class.java.hashCode() -> {
                GlucoseIndexesViewHolder(
                    ItemStatGlucoseIndexesSliderBinding.inflate(inflater, parent, false),
                    viewPool,
                    GlucoseItemGroupAdapter()
                )
            }
            GeneralIndexItem::class.java.hashCode() -> {
                GeneralIndexViewHolder(
                    ItemStatGeneralIndexBinding.inflate(inflater, parent, false)
                )
            }
            else -> throw IllegalArgumentException("No delegate defined for ${this::class.simpleName}")
        }
    }
}
