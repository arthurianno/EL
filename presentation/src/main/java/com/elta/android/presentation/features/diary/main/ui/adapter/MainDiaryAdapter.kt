package com.elta.android.presentation.features.diary.main.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseViewHolder
import com.elta.android.presentation.core.ui.adapter.DefaultDiffCallback
import com.elta.android.presentation.databinding.ItemRecordBinding
import com.elta.android.presentation.databinding.ItemRecordsGroupBinding
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsGroupItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class MainDiaryAdapter @Inject constructor(
    private val viewPool: RecyclerView.RecycledViewPool,
    private val bus: RxBus
) : BaseListAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            RecordItem::class.java.hashCode() -> {
                ItemRecordViewHolder(ItemRecordBinding.inflate(inflater, parent, false), bus)
            }
            RecordsGroupItem::class.java.hashCode() -> {
                ItemRecordsGroupViewHolder(
                    ItemRecordsGroupBinding.inflate(inflater, parent, false),
                    viewPool,
                    RecordItemGroupAdapter(bus)
                )
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
        @Suppress("UNCHECKED_CAST")
        (holder as? BaseViewHolder<ListItem>)?.bind(getItem(position))
    }
}

abstract class BaseListAdapter :
    ListAdapter<ListItem, RecyclerView.ViewHolder>(DefaultDiffCallback())
