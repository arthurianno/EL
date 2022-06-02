package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.delegates

import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.items.HemoglobinHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import kotlinx.android.synthetic.main.item_hemoglobin_header.*

class HemoglobinHeaderDelegate : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_hemoglobin_header
    override val itemType: Any = HemoglobinHeaderItem::class

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as HemoglobinHeaderItem
        with(holder as ViewHolder) {
            headerTitleView.text = item.title
        }
    }
}
