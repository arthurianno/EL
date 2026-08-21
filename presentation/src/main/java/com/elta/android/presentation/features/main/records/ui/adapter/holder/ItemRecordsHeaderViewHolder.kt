package com.elta.android.presentation.features.main.records.ui.adapter.holder

import androidx.compose.ui.platform.ViewCompositionStrategy
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemRecordsHeaderBinding
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseDashboardScreen
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseState
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseTrend
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseTrendDirection
import com.nullgr.core.rx.RxBus
import com.elta.android.domain.features.diary.events.model.glucoseValue
import java.util.Locale
import kotlin.math.abs

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
        val rawGlucose = item.glucoseLevel?.format()?.takeIf { it.isNotBlank() } ?: "—"
        val numericValue = rawGlucose.replace(",", ".").toFloatOrNull()

        val glucoseState = item.dailyGlucoseModel?.glucoseLevelSettings?.let { settings ->
            when {
                numericValue == null -> GlucoseState.NORMAL
                numericValue.toDouble() in settings.low -> GlucoseState.LOW
                numericValue.toDouble() in settings.high -> GlucoseState.HIGH
                else -> GlucoseState.NORMAL
            }
        } ?: when {
            numericValue != null && numericValue < 3.9f -> GlucoseState.LOW
            numericValue != null && numericValue > 10.0f -> GlucoseState.HIGH
            else -> GlucoseState.NORMAL
        }
        val tirPercentage = item.dailyGlucoseModel?.let { model ->
            val values = model.glucoseEvents.map { it.glucoseValue(model.glucoseFormat) }
            val inRange = values.count { it in model.glucoseLevelSettings.normal }
            values.takeIf { it.size >= 2 }?.let { "${inRange * 100 / it.size}%" } ?: "—"
        } ?: "—"

        binding.composeHeaderView.setContent {
            GlucoseDashboardScreen(
                bus = bus,
                glucoseValue = rawGlucose,
                deltaText = item.glucoseLevelIndex?.format()?.takeIf { it.isNotBlank() } ?: "—",
                glucoseTrend = item.calculateGlucoseTrend(),
                tirPercentage = tirPercentage,
                initialGlucoseState = glucoseState,
                breadUnitsText = item.breadLevel?.let { "$it XE" } ?: "—",
                insulinText = item.insulinLevel?.let { "$it ед" } ?: "—",
                dailyGlucoseModel = item.dailyGlucoseModel,
                allDayEvents = item.allEvents
            )
        }
    }

    private fun RecordsHeaderItem.calculateGlucoseTrend(): GlucoseTrend? {
        val model = dailyGlucoseModel ?: return null
        val glucoseEvents = model.glucoseEvents.sortedBy { it.additionTime }
        if (glucoseEvents.size < 2) return null

        val currentValue = glucoseEvents.last().glucoseValue(model.glucoseFormat)
        val previousValue = glucoseEvents[glucoseEvents.lastIndex - 1].glucoseValue(model.glucoseFormat)
        val diff = currentValue - previousValue
        val direction = when {
            diff > 0.0 -> GlucoseTrendDirection.UP
            diff < 0.0 -> GlucoseTrendDirection.DOWN
            else -> GlucoseTrendDirection.STABLE
        }

        return GlucoseTrend(
            direction = direction,
            valueText = String.format(Locale.US, "%.1f", abs(diff)).replace('.', ',')
        )
    }
}
