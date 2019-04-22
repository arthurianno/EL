package com.elta.android.presentation.features.statistic.period.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import kotlinx.android.synthetic.main.item_stat_glucose_index.*

class GlucoseIndexDelegate : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_stat_glucose_index
    override val itemType: Any = GlucoseIndexItem::class

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as GlucoseIndexItem

        with(holder as ViewHolder) {
            itemView.background = item.bg
            indexValueView.text = item.value
            indexUnitView.text = item.unit
            indexDescriptionView.text = item.description
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payload: Any) {
        val item = items[position] as GlucoseIndexItem
        with(holder as ViewHolder) {
            when (payload) {
                GlucoseIndexItem.Payload.TYPE_CHANGED -> {
                    itemView.background = item.bg
                    indexDescriptionView.text = item.description
                    indexUnitView.text = item.unit
                }
                GlucoseIndexItem.Payload.VALUE_CHANGED -> {
                    itemView.background = item.bg
                    indexValueView.text = item.value
                }
            }
        }
    }
}