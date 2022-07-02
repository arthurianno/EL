package com.elta.android.presentation.features.statistic.period.ui.holder

import android.widget.TextView
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemStatGeneralIndexBinding
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GeneralIndexItem
import com.nullgr.core.font.getTypeface
import com.nullgr.core.font.toSpannable
import com.nullgr.core.font.typeface
import com.nullgr.core.ui.extensions.toggleView

class GeneralIndexViewHolder(
    private val binding: ItemStatGeneralIndexBinding
) : BaseListItemViewHolder<GeneralIndexItem>(binding.root) {
    override fun bind(item: GeneralIndexItem) {
        with(binding) {
            generalIndexIconView.setImageResource(item.icon)
            generalIndexTitleView.text = item.title
            setDescription(generalIndexDescriptionView, item)
            bottomDividerView.toggleView(item.isTheLast)
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
