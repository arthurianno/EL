package com.elta.android.presentation.features.main.records.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.adapter.GroupItem
import com.elta.android.presentation.core.ui.adapter.ParentAdapterDelegate
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.databinding.ItemRecordsGroupBinding
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsGroupItem
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.ViewHolder
import net.cachapa.expandablelayout.ExpandableLayout

class RecordsGroupDelegate(
    private val viewPool: RecyclerView.RecycledViewPool,
    factory: AdapterDelegatesFactory
) : ParentAdapterDelegate<ItemRecordsGroupBinding>(factory, ItemRecordsGroupBinding::inflate) {

    override val layoutResource: Int = R.layout.item_records_group
    override val itemType: Any = RecordsGroupItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                binding.run {
                    itemsView.layoutManager = FixedLinearLayoutManager(itemView.context)
                    itemsView.setRecycledViewPool(viewPool)
                    groupStateView.setOnClickListener {
                        withAdapterPosition<RecordsGroupItem> { _, item, _ ->
                            item.isExpanded = !itemsContainerView.isExpanded
                            toggleState(itemsContainerView, true, groupStateView, item)
                        }
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
        val item = items[position] as RecordsGroupItem
        with(binding) {
            groupIconView.setImageResource(item.icon)
            groupNameView.text = item.name
            toggleState(itemsContainerView, false, groupStateView, item)
            setItems(itemsView, true, item)
        }
    }

    private fun toggleState(
        layout: ExpandableLayout,
        animate: Boolean,
        indicator: View,
        item: GroupItem
    ) {
        layout.setExpanded(item.isExpanded, animate)
        indicator.isSelected = item.isExpanded
    }
}
