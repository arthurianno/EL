package com.elta.android.presentation.features.observers.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.elta.android.presentation.R
import com.elta.android.presentation.features.observers.ui.adapter.items.ObserverContentItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import kotlinx.android.synthetic.main.item_observer_content.*

class ObserverContentItemDelegate(
    private val bus: RxBus
) : AdapterDelegate() {
    override val itemType = ObserverContentItem::class
    override val layoutResource = R.layout.item_observer_content

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                functionStateView.setOnClickListener {
                }
            }
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        super.onBindViewHolder(items, position, holder)
        val item = items[position] as ObserverContentItem
        with(holder as ViewHolder) {
            functionNameView.text = item.title
            functionDescriptionNameView.text = item.email
        }
    }
}