package com.elta.android.presentation.features.main.records.mapper

import android.graphics.drawable.Drawable
import android.util.Log
import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.diary.home.model.GlucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelDirection
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.domain.features.cgm.repository.NmgRepository
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.records.ui.compose.NmgDashboardSummary
import com.elta.android.presentation.features.main.records.ui.compose.GlucosePoint
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsDailyGlucoseItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.date.CommonFormats
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class MainRecordsMapper @Inject constructor(
    resources: ResourceProvider,
    private val nmgRepository: NmgRepository
) : BaseRecordsMapper(resources), Mapper<HomeModel, List<ListItem>> {

    override fun mapFromObject(source: HomeModel): List<ListItem> =
        listOf(source.header())


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
            breadLevel = breadUnitsTotal.format(),
            insulinLevel = insulinTotal.format(),
            glucoseFormat = glucoseFormat,
            calculatorFlow = calculatorFlow,
            dailyGlucoseModel = dailyGlucoseModel,
            allEvents = eventsBlocks.flatMap { it.events },
            nmgSummary = nmgSummary()
        )

    private fun nmgSummary(): NmgDashboardSummary? {
        val sensorId = nmgRepository.activeSensorId() ?: return null
        val measurements = nmgRepository.measurements()
        Log.i(TAG, "NMG_DASHBOARD sensorId=$sensorId measurements=${measurements.size}")
        val latest = measurements.lastOrNull()
        return NmgDashboardSummary(
            sensorId = sensorId,
            isPrimary = nmgRepository.isPrimary(),
            latestReading = latest?.glucoseSignalNanoAmp?.toNmgDisplayValue(),
            updatedAtText = latest?.receivedAtEpochMillis
                ?.let { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it)) }
                ?: "Ожидаем данные",
            historySize = measurements.size,
            // A chart point represents one completed two-minute sensor interval. The latest
            // point is rendered separately as the active marker, so the gauge and chart always
            // show the same confirmed value rather than every BLE advertisement.
            points = measurements.dropLast(1).map { measurement ->
                GlucosePoint(
                    timeLabel = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(measurement.receivedAtEpochMillis)),
                    value = measurement.glucoseSignalNanoAmp.toNmgDisplayValue(),
                    receivedAtEpochMillis = measurement.receivedAtEpochMillis
                )
            },
            livePoint = latest?.let { measurement ->
                GlucosePoint(
                    timeLabel = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(measurement.receivedAtEpochMillis)),
                    value = measurement.glucoseSignalNanoAmp.toNmgDisplayValue(),
                    receivedAtEpochMillis = measurement.receivedAtEpochMillis
                )
            }
        )
    }

    private companion object {
        const val TAG = "NmgMonitoring"
        const val NMG_DISPLAY_DIVISOR = 10_000f
    }

    private fun Int.toNmgDisplayValue(): Float = this / NMG_DISPLAY_DIVISOR

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
