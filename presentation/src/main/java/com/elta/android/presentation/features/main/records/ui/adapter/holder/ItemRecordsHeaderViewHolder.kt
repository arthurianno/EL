package com.elta.android.presentation.features.main.records.ui.adapter.holder

import androidx.compose.ui.platform.ViewCompositionStrategy
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemRecordsHeaderBinding
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseDashboardScreen
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseState
import com.nullgr.core.rx.RxBus

class ItemRecordsHeaderViewHolder(
    private val binding: ItemRecordsHeaderBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<RecordsHeaderItem>(binding.root) {

    init {
        binding.composeHeaderView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
    }

    override fun bind(item: RecordsHeaderItem) {
        val rawGlucose = item.glucoseLevel?.format() ?: "4,1"
        val numericValue = rawGlucose.replace(",", ".").toFloatOrNull() ?: 4.1f

        val glucoseState = when {
            numericValue < 3.9f -> GlucoseState.LOW
            numericValue > 10.0f -> GlucoseState.HIGH
            else -> GlucoseState.NORMAL
        }

        binding.composeHeaderView.setContent {
            GlucoseDashboardScreen(
                bus = bus,
                glucoseValue = rawGlucose,
                deltaText = item.glucoseLevelIndex?.format() ?: "▼2,4",
                initialGlucoseState = glucoseState,
                breadUnitsText = item.breadLevel?.let { "$it XE" } ?: "0,9 Ед.",
                insulinText = item.insulinLevel?.let { "$it ед" } ?: "0,1 ХЕ",
                dailyGlucoseModel = item.dailyGlucoseModel,
                allDayEvents = item.allEvents
            )
        }
    }
}

