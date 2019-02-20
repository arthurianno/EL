package com.elta.android.presentation.features.shops.map.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.bindCardCorners
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchResultItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import kotlinx.android.synthetic.main.item_search_result.*

class SearchResultDelegate(
    private val bus: RxBus
) : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_search_result
    override val itemType: Any = SearchResultItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemView.setOnClickListener {
                    withAdapterPosition<SearchResultItem> { _, item, _ ->
                        bus.click(Clicks.SearchResult(item))
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        bindCardCorners(items, position, holder)
        val item = items[position] as SearchResultItem

        with(holder as ViewHolder) {
            nameView.text = item.name
            addressView.text = item.address
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payload: Any) {
        when (payload) {
            SearchResultItem.Payload.CARD_TYPE_CHANGED -> bindCardCorners(items, position, holder)
        }
    }
}