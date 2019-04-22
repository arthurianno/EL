package com.elta.android.presentation.features.statistic.period.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GeneralIndexItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.font.getTypeface
import com.nullgr.core.font.toSpannable
import com.nullgr.core.font.typeface
import com.nullgr.core.ui.extensions.toggleView
import kotlinx.android.synthetic.main.item_record.*
import kotlinx.android.synthetic.main.item_stat_general_index.*

class GeneralIndexDelegate : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_stat_general_index
    override val itemType: Any = GeneralIndexItem::class

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as GeneralIndexItem

        with(holder as ViewHolder) {
            generalIndexIconView.setImageResource(item.icon)
            generalIndexTitleView.text = item.title
            setDescription(generalIndexDescriptionView, item)
            bottomDividerView.toggleView(item.isTheLast)
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payload: Any) {
        val item = items[position] as GeneralIndexItem
        with(holder as ViewHolder) {
            when (payload) {
                GeneralIndexItem.Payload.ICON_CHANGED -> generalIndexIconView.setImageResource(item.icon)
                GeneralIndexItem.Payload.TITLE_CHANGED -> recordTitleView.text = item.title
                GeneralIndexItem.Payload.DESCRIPTION_CHANGED -> setDescription(generalIndexDescriptionView, item)
                GeneralIndexItem.Payload.POSITION_CHANGED -> bottomDividerView.toggleView(item.isTheLast)
            }
        }
    }

    private fun setDescription(view: TextView, item: GeneralIndexItem) {
        view.text = item.description.toSpannable()
            .typeface {
                typeface = view.context.getTypeface("roboto_medium.ttf")
                toText = item.value
            }
    }
}