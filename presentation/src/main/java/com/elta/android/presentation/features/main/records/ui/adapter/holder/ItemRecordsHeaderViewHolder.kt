package com.elta.android.presentation.features.main.records.ui.adapter.holder

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemRecordsHeaderBinding
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.nullgr.core.ui.extensions.toggleView

class ItemRecordsHeaderViewHolder(
    private val binding: ItemRecordsHeaderBinding
) : BaseListItemViewHolder<RecordsHeaderItem>(binding.root) {

    override fun bind(item: RecordsHeaderItem) {
        bindGlucose(item)
        bindBread(item)
        bindInsulin(item)
    }

    private fun bindGlucose(item: RecordsHeaderItem) {
        with(binding) {
            glucoseEmptyValueView.toggleView(item.glucoseLevel == null)
            glucoseValueContainerView.toggleView(item.glucoseLevel != null)

            item.glucoseLevel?.let { glucoseLevelValueView.text = it.format() }

            glucoseLevelDirectionView.toggleView(item.glucoseLevelIndex != null)
            item.glucoseLevelIndex?.let { glucoseLevelChangeIndexView.text = it.format() }
            item.glucoseLevelIndexIcon?.let { glucoseLevelChangeIndexIconView.setImageResource(it) }

            root.background = item.background
        }
    }

    private fun bindBread(item: RecordsHeaderItem) {
        binding.breadValueView.text = item.breadLevel.formatAsValueOrEmpty()
    }

    private fun bindInsulin(item: RecordsHeaderItem) {
        binding.insulinValueView.text = item.insulinLevel.formatAsValueOrEmpty()
    }

    private fun String?.formatAsValueOrEmpty(): String =
        when {
            this != null -> this@ItemRecordsHeaderViewHolder.binding.root.context.getString(
                R.string.main_records_mask_value_he,
                this
            )
            else -> this@ItemRecordsHeaderViewHolder.binding.root.context.getString(R.string.main_records_empty_value)
        }
}
