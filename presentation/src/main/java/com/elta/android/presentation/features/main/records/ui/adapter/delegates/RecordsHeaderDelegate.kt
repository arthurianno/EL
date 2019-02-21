package com.elta.android.presentation.features.main.records.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.records.models.GlucoseRange
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.ui.extensions.toggleView
import kotlinx.android.synthetic.main.item_records_header.*
import java.text.DecimalFormat

class RecordsHeaderDelegate(private val resourceProvider: ResourceProvider) : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_records_header
    override val itemType: Any = RecordsHeaderItem::class

    private val numberFormat by lazy { DecimalFormat("#.#") }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as RecordsHeaderItem
        holder as ViewHolder
        bindGlucose(holder, item)
        bindBread(holder, item)
        bindInsulin(holder, item)
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payload: Any) {
        val item = items[position] as RecordsHeaderItem
        holder as ViewHolder
        when (payload) {
            RecordsHeaderItem.Payload.GLUCOSE_LEVEL_CHANGED -> bindGlucose(holder, item)
            RecordsHeaderItem.Payload.BREAD_LEVEL_CHANGED -> bindBread(holder, item)
            RecordsHeaderItem.Payload.INSULIN_CHANGED -> bindInsulin(holder, item)
        }
    }

    private fun bindGlucose(holder: ViewHolder, item: RecordsHeaderItem) {
        with(holder) {
            glucoseEmptyValueView.toggleView(item.glucoseLevel == null)
            glucoseValueContainerView.toggleView(item.glucoseLevel != null)

            item.glucoseLevel?.let { glucoseLevelValueView.text = it.format() }
            item.glucoseLevelIndex?.let { glucoseLevelChangeIndexView.text = it.format() }
            item.glucoseLevelIndexIcon?.let { glucoseLevelChangeIndexIconView.setImageResource(it) }

            itemView.setBackgroundResource(item.glucoseLevel.glucoseToBackground())
        }
    }

    private fun bindBread(holder: ViewHolder, item: RecordsHeaderItem) {
        with(holder) {
            breadValueView.text = item.breadLevel.formatAsValueOrEmpty()
        }
    }

    private fun bindInsulin(holder: ViewHolder, item: RecordsHeaderItem) {
        with(holder) {
            insulinValueView.text = item.insulinLevel.formatAsValueOrEmpty()
        }
    }

    private fun Double.format(): String = numberFormat.format(this)

    private fun Double?.formatAsValueOrEmpty(): String =
        when {
            this != null -> resourceProvider.getString(R.string.main_records_mask_value, this.format())
            else -> resourceProvider.getString(R.string.main_records_empty_value)
        }

    private fun Double?.glucoseToBackground(): Int =
        when {
            this == null || this in GlucoseRange.MEDIUM -> R.drawable.bg_gradient_green
            this in GlucoseRange.HIGH -> R.drawable.bg_gradient_red
            else -> R.drawable.bg_gradient_blue
        }
}