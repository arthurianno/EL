package com.elta.android.presentation.features.main.records.ui.adapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.core.ui.adapter.GroupItem
import com.elta.android.presentation.databinding.ItemRecordsGroupBinding
import com.elta.android.presentation.features.main.records.ui.adapter.RecordItemGroupAdapter
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsGroupItem
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import net.cachapa.expandablelayout.ExpandableLayout

class ItemRecordsGroupViewHolder(
    private val binding: ItemRecordsGroupBinding,
    viewPool: RecyclerView.RecycledViewPool,
    private val adapter: RecordItemGroupAdapter
) : BaseListItemViewHolder<RecordsGroupItem>(binding.root) {

    init {
        with(binding) {
            itemsView.layoutManager = FixedLinearLayoutManager(itemView.context)
            itemsView.setRecycledViewPool(viewPool)
            itemsView.adapter = adapter
        }
    }

    override fun bind(item: RecordsGroupItem) {
        with(binding) {
            groupIconView.setImageResource(item.icon)
            groupNameView.text = item.name
            toggleState(itemsContainerView, false, groupStateView, item)
            adapter.submitList(item.items)
            groupStateView.setOnClickListener {
                item.isExpanded = !itemsContainerView.isExpanded
                toggleState(itemsContainerView, true, groupStateView, item)
            }
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
