package com.elta.android.presentation.features.diary.main.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.DefaultDiffCallback
import com.elta.android.presentation.core.ui.adapter.GroupItem
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.databinding.ItemRecordBinding
import com.elta.android.presentation.databinding.ItemRecordsGroupBinding
import com.elta.android.presentation.features.main.records.ui.adapter.delegates.RecordDelegate
import com.elta.android.presentation.features.main.records.ui.adapter.delegates.RecordsGroupDelegate
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsGroupItem
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleView
import net.cachapa.expandablelayout.ExpandableLayout
import javax.inject.Inject

class MainDiaryDelegatesFactory @Inject constructor(
    private val viewPool: RecyclerView.RecycledViewPool,
    private val bus: RxBus
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            RecordItem::class.java -> RecordDelegate(bus)
            RecordsGroupItem::class.java -> RecordsGroupDelegate(viewPool, this)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}

class MainDiaryAdapter @Inject constructor(
    private val viewPool: RecyclerView.RecycledViewPool,
    private val bus: RxBus
) : ListAdapter<ListItem, RecyclerView.ViewHolder>(DefaultDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            RecordItem::class.java.hashCode() -> {
                RecordItemViewHolder(ItemRecordBinding.inflate(inflater, parent, false), bus)
            }
            RecordsGroupItem::class.java.hashCode() -> {
                ItemRecordsGroupViewHolder(ItemRecordsGroupBinding.inflate(inflater, parent, false))
            }
            else -> {
                throw IllegalArgumentException("No delegate defined for ${viewType::class.simpleName}")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)::class.java.hashCode()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}

class RecordItemViewHolder(
    private val binding: ItemRecordBinding,
    private val bus: RxBus
) : RecyclerView.ViewHolder(binding.root) {
    init {
        itemView.setOnClickListener {
            withAdapterPosition<RecordItem> { _, item, _ ->
                bus.click(Clicks.RecordClicked(item))
            }
        }
    }

    fun bind(item: RecordItem) {
        with(binding) {
            recordIconView.setImageResource(item.icon)
            recordTitleView.text = item.title
            recordTypeView.text = item.type
            recordCountView.text = item.count
            recordDateView.text = item.date
            recordLabelView.toggleView(item.showLabel)
        }
    }
}

class ItemRecordsGroupViewHolder(
    private val binding: ItemRecordsGroupBinding
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.run {
            itemsView.layoutManager = FixedLinearLayoutManager(itemView.context)
            itemsView.setRecycledViewPool(RecyclerView.RecycledViewPool())
            groupStateView.setOnClickListener {
                withAdapterPosition<RecordsGroupItem> { _, item, _ ->
                    item.isExpanded = !itemsContainerView.isExpanded
                    toggleState(itemsContainerView, true, groupStateView, item)
                }
            }
        }
    }

    fun bind(item: RecordsGroupItem) {
        with(binding) {
            groupIconView.setImageResource(item.icon)
            groupNameView.text = item.name
            toggleState(itemsContainerView, false, groupStateView, item)
//            itemsView.adapter?.
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
