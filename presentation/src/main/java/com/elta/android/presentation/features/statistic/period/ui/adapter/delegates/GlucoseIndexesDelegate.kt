package com.elta.android.presentation.features.statistic.period.ui.adapter.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.adapter.ParentAdapterDelegate
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexesItem
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.ViewHolder
import kotlinx.android.synthetic.main.item_records_group.*

class GlucoseIndexesDelegate(
    private val viewPool: RecyclerView.RecycledViewPool,
    factory: AdapterDelegatesFactory
) : ParentAdapterDelegate(factory) {

    override val layoutResource: Int = R.layout.item_stat_glucose_indexes_slider
    override val itemType: Any = GlucoseIndexesItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemsView.layoutManager =
                    FixedLinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL)
                itemsView.setRecycledViewPool(viewPool)
            }
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as GlucoseIndexesItem

        with(holder as ViewHolder) {
            setItems(itemsView, true, item)
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payload: Any
    ) {
        val item = items[position] as GlucoseIndexesItem
        with(holder as ViewHolder) {
            when (payload) {
                GlucoseIndexesItem.Payload.ITEMS_CHANGED -> setItems(itemsView, true, item)
            }
        }
    }
}
