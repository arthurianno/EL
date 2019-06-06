package com.elta.android.presentation.features.statistic.period.ui.adapter.delegates

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.elta.android.presentation.R
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexesItem
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.DynamicAdapter
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import kotlinx.android.synthetic.main.item_records_group.*

class GlucoseIndexesDelegate(
    private val factory: AdapterDelegatesFactory,
    private val calculator: DiffCalculator,
    private val viewPool: RecyclerView.RecycledViewPool
) : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_stat_glucose_indexes_slider
    override val itemType: Any = GlucoseIndexesItem::class

    private val adapters = mutableMapOf<Any, DynamicAdapter>()

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemsView.layoutManager = FixedLinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL)
                itemsView.setRecycledViewPool(viewPool)
            }
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as GlucoseIndexesItem

        with(holder as ViewHolder) {
            setItems(itemsView, false, item)
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payload: Any) {
        val item = items[position] as GlucoseIndexesItem
        with(holder as ViewHolder) {
            when (payload) {
                GlucoseIndexesItem.Payload.ITEMS_CHANGED -> setItems(itemsView, true, item)
            }
        }
    }

    private fun setItems(view: RecyclerView, useDiffUtils: Boolean, item: GlucoseIndexesItem) {
        val adapter = createOrGetAdapter(item)
        view.adapter = adapter
        adapter.updateData(item.items, useDiffUtils)
    }

    private fun createOrGetAdapter(item: ListItem): DynamicAdapter = adapters[item.getUniqueProperty()]
        ?: DynamicAdapter(factory, calculator).also { adapters[item.getUniqueProperty()] = it }
}