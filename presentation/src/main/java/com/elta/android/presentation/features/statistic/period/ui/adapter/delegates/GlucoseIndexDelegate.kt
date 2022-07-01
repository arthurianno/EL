package com.elta.android.presentation.features.statistic.period.ui.adapter.delegates

import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.ItemStatGlucoseIndexBinding
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder

class GlucoseIndexDelegate :
    AdapterDelegate<ItemStatGlucoseIndexBinding>(ItemStatGlucoseIndexBinding::inflate) {

    override val layoutResource: Int = R.layout.item_stat_glucose_index
    override val itemType: Any = GlucoseIndexItem::class

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as GlucoseIndexItem

        with(holder as ViewHolder) {
            binding.run {
                itemView.background = item.bg
                indexValueView.text = item.value
                indexUnitView.text = item.unit
                indexDescriptionView.text = item.description
            }
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payload: Any
    ) {
        val item = items[position] as GlucoseIndexItem
        with(holder as ViewHolder) {
            binding.run {
                when (payload) {
                    GlucoseIndexItem.Payload.TYPE_CHANGED ->
                        indexDescriptionView.text =
                            item.description
                    GlucoseIndexItem.Payload.VALUE_CHANGED -> indexValueView.text = item.value
                    GlucoseIndexItem.Payload.BG_CHANGED -> itemView.background = item.bg
                    GlucoseIndexItem.Payload.UNIT_CHANGED -> indexUnitView.text = item.unit
                }
            }
        }
    }
}
