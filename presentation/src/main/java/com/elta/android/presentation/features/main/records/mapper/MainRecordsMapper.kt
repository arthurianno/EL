package com.elta.android.presentation.features.main.records.mapper

import android.graphics.drawable.Drawable
import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.diary.home.model.GlucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelDirection
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsDailyGlucoseItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.date.CommonFormats
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class MainRecordsMapper @Inject constructor(
    resources: ResourceProvider
) : BaseRecordsMapper(resources), Mapper<HomeModel, List<ListItem>> {

    override fun mapFromObject(source: HomeModel): List<ListItem> =
        arrayListOf<ListItem>().apply {
            if (source.hasEvents) {
                add(source.header())
            }
            if (source.dailyGlucoseModel.hasEvents) {
                add(source.dailyChart())
            }
            addAll(source.eventsBlocks.flatMapIndexed { index, event ->
                event.ungroup(index == 0, source.calculatorFlow)
            })
        }

    private fun HomeModel.dailyChart(): RecordsDailyGlucoseItem {
        val lastEventTime =
            dailyGlucoseModel.lastEvent?.additionTime?.toStringWithFormat(CommonFormats.FORMAT_TIME)
        return RecordsDailyGlucoseItem(
            ChartItemsBuilder.build(dailyGlucoseModel),
            resources.getString(R.string.main_records_daily_glucose_subtitle, lastEventTime.orEmpty())
        )
    }

    private fun HomeModel.header(): ListItem =
        RecordsHeaderItem(
            background = glucoseLevel.toBackground(),
            glucoseLevel = lastGlucoseEvent?.value.format(),
            glucoseLevelIndex = glucoseLevelDifference.format(),
            glucoseLevelIndexIcon = this.glucoseLevelDirection?.icon(),
            breadLevel = lastFoodEvent?.value.format(),
            insulinLevel = lastInsulinEvent?.value.format(),
            glucoseFormat = glucoseFormat,
            calculatorFlow = calculatorFlow
        )

    private fun GlucoseLevelDirection.icon(): Int? =
        when (this) {
            GlucoseLevelDirection.UP -> R.drawable.ic_change_index_up
            GlucoseLevelDirection.DOWN -> R.drawable.ic_change_index_down
            else -> null
        }

    private fun GlucoseLevel?.toBackground(): Drawable? =
        when {
            this == null -> resources.getDrawable(R.drawable.bg_gradient_green)
            this == GlucoseLevel.HIGH -> resources.getDrawable(R.drawable.bg_gradient_red)
            this == GlucoseLevel.LOW -> resources.getDrawable(R.drawable.bg_gradient_blue)
            else -> resources.getDrawable(R.drawable.bg_gradient_green)
        }
}
