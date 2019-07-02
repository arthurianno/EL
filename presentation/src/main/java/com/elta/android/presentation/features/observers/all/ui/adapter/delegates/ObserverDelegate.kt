package com.elta.android.presentation.features.observers.all.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.elta.android.domain.features.observers.model.ObserverStatus
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import kotlinx.android.synthetic.main.item_profile_additional_settings.*

class ObserverDelegate(
    private val bus: RxBus
) : AdapterDelegate() {

    override val itemType = ObserverItem::class
    override val layoutResource = R.layout.item_profile_additional_settings

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemView.setOnClickListener {
                    withAdapterPosition<ObserverItem> { _, item, _ ->
                        bus.click(Clicks.ObserverItemClicked(item))
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as ObserverItem
        with(holder as ViewHolder) {
            fillItem(item)
            settingsDescriptionNameView.text = item.description
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payload: Any) {
        val item = items[position] as ObserverItem
        with(holder as ViewHolder) {
            when (payload) {
                ObserverItem.Payload.STATUS_CHANGED, ObserverItem.Payload.TITLE_CHANGED ->
                    fillItem(item)
            }
        }
    }

    private fun ViewHolder.fillItem(item: ObserverItem) {
        settingsTypeIconView.setImageResource(item.type)
        settingsActionIconView.setImageResource(item.action)
        settingsNameView.text = item.title

        when (item.status) {
            ObserverStatus.CONFIRMED -> {
                settingsActionIconView.visibility = View.VISIBLE
                itemView.isClickable = true
            }
            else -> {
                settingsActionIconView.visibility = View.GONE
                itemView.isClickable = false
            }
        }
    }
}