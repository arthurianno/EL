package com.elta.android.presentation.features.main.records.ui.adapter.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.databinding.ItemRecordBinding
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleView

class RecordDelegate(
    private val bus: RxBus
) : AdapterDelegate<ItemRecordBinding>(ItemRecordBinding::inflate) {

    override val layoutResource: Int = R.layout.item_record
    override val itemType: Any = RecordItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemView.setOnClickListener {
                    withAdapterPosition<RecordItem> { _, item, _ ->
                        bus.click(Clicks.RecordClicked(item))
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
        val item = items[position] as RecordItem
        with(binding) {
            recordIconView.setImageResource(item.icon)
            recordTitleView.text = item.title
            recordTypeView.text = item.type
            recordCountView.text = item.count
            recordDateView.text = item.date
            recordLabelView.toggleView(item.showLabel)
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payload: Any
    ) {
        val item = items[position] as RecordItem
        with(binding) {
            when (payload) {
                RecordItem.Payload.ICON_CHANGED -> recordIconView.setImageResource(item.icon)
                RecordItem.Payload.TITLE_CHANGED -> recordTitleView.text = item.title
                RecordItem.Payload.TYPE_CHANGED -> recordTypeView.text = item.type
                RecordItem.Payload.COUNT_CHANGED -> recordCountView.text = item.count
                RecordItem.Payload.DATE_CHANGED -> recordDateView.text = item.date
                RecordItem.Payload.LABEL_CHANGED -> recordLabelView.toggleView(item.showLabel)
            }
        }
    }
}
