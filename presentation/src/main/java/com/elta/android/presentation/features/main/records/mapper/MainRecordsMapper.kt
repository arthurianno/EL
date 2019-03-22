package com.elta.android.presentation.features.main.records.mapper

import android.graphics.drawable.Drawable
import com.elta.android.common.mapper.Mapper
import com.elta.android.domain.features.diary.home.model.GlucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelDirection
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class MainRecordsMapper @Inject constructor(
    resources: ResourceProvider
) : BaseRecordsMapper(resources), Mapper<HomeModel, List<ListItem>> {

    override fun mapFromObject(source: HomeModel): List<ListItem> =
        arrayListOf<ListItem>().apply {
            add(source.header())
            addAll(source.eventsBlocks.mapIndexed { index, event -> event.group(index == 0) })
        }

    private fun HomeModel.header(): ListItem =
        RecordsHeaderItem(
            background = glucoseLevel.toBackground(),
            glucoseLevel = lastGlucoseEvent?.value.format(),
            glucoseLevelIndex = glucoseLevelDifference.format(),
            glucoseLevelIndexIcon = this.glucoseLevelDirection?.icon(),
            breadLevel = lastBreadEvent?.value.format(),
            insulinLevel = lastInsulinEvent?.value.format()
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