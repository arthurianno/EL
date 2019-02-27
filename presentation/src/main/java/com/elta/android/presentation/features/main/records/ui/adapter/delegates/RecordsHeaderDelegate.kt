package com.elta.android.presentation.features.main.records.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleView
import kotlinx.android.synthetic.main.item_records_header.*

class RecordsHeaderDelegate(
    private val bus: RxBus,
    private val resources: ResourceProvider
) : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_records_header
    override val itemType: Any = RecordsHeaderItem::class

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

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        bus.event(Events.RecordsAttachedStateChanged(true))
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        bus.event(Events.RecordsAttachedStateChanged(false))
    }

    private fun bindGlucose(holder: ViewHolder, item: RecordsHeaderItem) {
        with(holder) {
            glucoseEmptyValueView.toggleView(item.glucoseLevel == null)
            glucoseValueContainerView.toggleView(item.glucoseLevel != null)

            item.glucoseLevel?.let { glucoseLevelValueView.text = it.format() }

            glucoseLevelDirectionView.toggleView(item.glucoseLevelIndex != null)
            item.glucoseLevelIndex?.let { glucoseLevelChangeIndexView.text = it.format() }
            item.glucoseLevelIndexIcon?.let { glucoseLevelChangeIndexIconView.setImageResource(it) }

            itemView.background = item.background
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

    private fun String?.formatAsValueOrEmpty(): String =
        when {
            this != null -> resources.getString(R.string.main_records_mask_value, this)
            else -> resources.getString(R.string.main_records_empty_value)
        }
}