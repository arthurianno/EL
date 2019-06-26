package com.elta.android.presentation.features.profile.support.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportActionItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import kotlinx.android.synthetic.main.item_support_action.*

class SupportActionDelegate(val bus: RxBus) : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_support_action
    override val itemType: Any = SupportActionItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                val listener = View.OnClickListener {
                    withAdapterPosition<SupportActionItem> { _, item, _ ->
                        bus.click(Clicks.SupportActionClicked(item.action))
                    }
                }
                actionView.setOnClickListener(listener)
            }
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as SupportActionItem
        with(holder as ViewHolder) {
            actionIconView.setImageResource(item.icon)
            actionNameView.text = item.title
            actionDescriptionNameView.text = item.subTitle
        }
    }
}