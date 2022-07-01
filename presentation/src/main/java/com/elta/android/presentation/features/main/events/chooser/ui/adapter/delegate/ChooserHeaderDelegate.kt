package com.elta.android.presentation.features.main.events.chooser.ui.adapter.delegate

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.ItemChooserHeaderBinding
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder

class ChooserHeaderDelegate :
    AdapterDelegate<ItemChooserHeaderBinding>(ItemChooserHeaderBinding::inflate) {

    override val layoutResource: Int = R.layout.item_chooser_header
    override val itemType: Any = ChooserHeaderItem::class

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as ChooserHeaderItem

        with(holder as ViewHolder) {
            (itemView as TextView).text = item.title
        }
    }
}
