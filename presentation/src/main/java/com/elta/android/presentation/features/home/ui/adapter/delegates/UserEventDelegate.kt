package com.elta.android.presentation.features.home.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.features.home.ui.adapter.items.UserEventItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import kotlinx.android.synthetic.main.item_user_event.*

class UserEventDelegate(private val bus: RxBus) : AdapterDelegate() {
    override val itemType = UserEventItem::class
    override val layoutResource = R.layout.item_user_event

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                eventContainerView.setOnClickListener {
                    withAdapterPosition<UserEventItem> { _, item, _ ->
                        bus.click(Clicks.AddUserEvent(item.event))
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        super.onBindViewHolder(items, position, holder)
        val item = items[position] as UserEventItem

        with(holder as ViewHolder) {
            eventIconView.setImageResource(item.iconRes)
            eventTitleView.setText(item.titleRes)
        }
    }
}