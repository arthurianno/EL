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
        bindGlucose(holder, item)
        bindXe(holder, item)
        bindInsulin(holder, item)
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payload: Any) {
        val item = items[position] as RecordsHeaderItem
        when (payload) {
            RecordsHeaderItem.Payload.GLUCOSE_LEVEL_CHANGED -> bindGlucose(holder, item)
            RecordsHeaderItem.Payload.XE_LEVEL_CHANGED -> bindXe(holder, item)
            RecordsHeaderItem.Payload.INSULIN_CHANGED -> bindInsulin(holder, item)
        }
    }

    private fun bindGlucose(holder: RecyclerView.ViewHolder, item: RecordsHeaderItem) {
        with(holder as ViewHolder) {
            glucoseEmptyValueView.toggleView(item.glucoseLevel == null)
            glucoseValueContainerView.toggleView(item.glucoseLevel != null)

            item.glucoseLevel?.let { glucoseLevelValueView.text = it.format() }
            item.glucoseLevelIndex?.let { glucoseLevelChangeIndexView.text = it.format() }
            item.glucoseLevelIndexDirection?.let {
                glucoseLevelChangeIndexIconView.setImageResource(it.toIcon())
            }

            itemView.setBackgroundResource(item.glucoseLevel.glucoseToBackground())
        }
    }

    private fun bindXe(holder: RecyclerView.ViewHolder, item: RecordsHeaderItem) {
        with(holder as ViewHolder) {
            xeValueView.text = item.xeLevel.formatAsValueOrEmpty()
        }
    }

    private fun bindInsulin(holder: RecyclerView.ViewHolder, item: RecordsHeaderItem) {
        with(holder as ViewHolder) {
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

    private fun RecordsHeaderItem.IndexDirection.toIcon(): Int =
        when (this) {
            RecordsHeaderItem.IndexDirection.UP -> R.drawable.ic_change_index_up
            else -> R.drawable.ic_change_index_down
        }
}