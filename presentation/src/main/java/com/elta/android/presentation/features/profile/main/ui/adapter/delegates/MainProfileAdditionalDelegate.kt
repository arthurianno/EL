package com.elta.android.presentation.features.profile.main.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileAdditionalItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleView
import kotlinx.android.synthetic.main.item_profile_functions.*

class MainProfileAdditionalDelegate(
    private val bus: RxBus
) : AdapterDelegate() {

    override val itemType = MainProfileAdditionalItem::class
    override val layoutResource = R.layout.item_profile_functions

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                val listener = View.OnClickListener {
                    withAdapterPosition<MainProfileAdditionalItem> { _, item, _ ->
                        bus.click(Clicks.ProfileAdditionalClicked(item))
                    }
                }
                functionView.setOnClickListener(listener)
            }
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as MainProfileAdditionalItem
        with(holder as ViewHolder) {
            functionIconView.setImageResource(item.icon)
            functionNameView.setText(item.title)
            item.description?.let { functionDescriptionNameView.setText(it) }
            functionDescriptionNameView.toggleView(item.description != null)
            functionStateView.toggleView(item.showGoArrow)
        }
    }
}
