package com.elta.android.presentation.features.main.records.ui.adapter.holder

import androidx.annotation.StringRes
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.user.model.GlucoseFormat
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
            glucoseLevelTitleView.setText(
                when (item.glucoseFormat) {
                    GlucoseFormat.CAPILLARY -> R.string.main_records_glucose_capillary
                    GlucoseFormat.PLASMA -> R.string.main_records_glucose_plasma
                }
            )
            root.background = item.background
        }
    }

    private fun bindBread(item: RecordsHeaderItem) {
        binding.breadUnitsLabelView.toggleView(item.calculatorFlow == CalculatorFlow.BREAD_UNITS)
        binding.breadValueView.text =
            item.breadLevel formatAsValueOrEmpty R.string.main_records_mask_value_he
    }

    private fun bindInsulin(item: RecordsHeaderItem) {
        binding.insulinValueView.text =
            item.insulinLevel formatAsValueOrEmpty R.string.main_records_mask_value
    }

    private infix fun String?.formatAsValueOrEmpty(@StringRes itemId: Int): String =
        this?.let { binding.root.context.getString(itemId, it) }
            ?: binding.root.context.getString(R.string.main_records_empty_value)
}
