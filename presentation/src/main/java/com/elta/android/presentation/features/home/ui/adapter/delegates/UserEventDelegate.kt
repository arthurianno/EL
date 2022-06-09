package com.elta.android.presentation.features.home.ui.adapter.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.databinding.ItemUserEventBinding
import com.elta.android.presentation.features.home.ui.adapter.items.UserEventItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus

class UserEventDelegate(private val bus: RxBus) :
    AdapterDelegate<ItemUserEventBinding>(ItemUserEventBinding::inflate) {
    override val itemType = UserEventItem::class
    override val layoutResource = R.layout.item_user_event

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                binding.eventContainerView.setOnClickListener {
                    withAdapterPosition<UserEventItem> { _, item, _ ->
                        bus.click(Clicks.AddUserEvent(item.meta))
                    }
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
        val item = items[position] as UserEventItem
        with(binding) {
            eventIconView.setImageResource(item.iconRes)
            eventTitleView.setText(item.titleRes)
        }
    }
}
