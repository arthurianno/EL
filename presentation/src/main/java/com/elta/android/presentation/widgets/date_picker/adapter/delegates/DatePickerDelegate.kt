package com.elta.android.presentation.widgets.date_picker.adapter.delegates

import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.date_picker.adapter.items.DatePickerItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import kotlinx.android.synthetic.main.item_date_picker.*

@Suppress("MagicNumber")
class DatePickerDelegate : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_date_picker
    override val itemType: Any = DatePickerItem::class

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as DatePickerItem

        with(holder as ViewHolder) {
            itemView.isSelected = false
            dayOfWeekTitleView.setText(item.dayOfWeekResId)
            dayOfMonthTitleView.text = item.dayOfMonth.toString()
            dayOfWeekTitleView.alpha = item.isAvailable.toAlpha()
            dayOfMonthTitleView.alpha = item.isAvailable.toAlpha()
        }
    }

    private fun Boolean.toAlpha() =
        if (this) 1f else 0.4f
}
