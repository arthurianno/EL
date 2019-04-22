@file:Suppress("MaxLineLength", "LongMethod")

package com.elta.android.presentation.features.statistic.period.pm

import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.elta.android.presentation.features.statistic.period.ui.Period
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GeneralIndexItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexesItem
import com.nullgr.core.adapter.items.ListItem
import javax.inject.Inject

class PeriodPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services) {

    val loadScreenAction = Action<Period>()

    override fun onCreate() {
        super.onCreate()

        loadScreenAction.observable
            .skipWhileInProgress()
            .doOnNext {
                items.consumer.accept(
                    arrayListOf<ListItem>().apply {
                        add(ProfileSettingsHeaderItem("$it"))
                        add(
                            GlucoseIndexesItem(
                                arrayListOf<ListItem>().apply {
                                    GlucoseIndexItem.Type.values().forEach {
                                        add(GlucoseIndexItem(it, resources.getDrawable(it.getBg()), "value", "unit", resources.getString(it.getDescription())))
                                    }
                                }
                            )
                        )
                        add(
                            GeneralIndexItem(
                                R.drawable.ic_event_bread_with_bg,
                                resources.getString(R.string.statistic_general_index_title_by_period_bread),
                                resources.getString(R.string.statistic_general_index_description_by_period_bread, "3"),
                                "3"
                            )
                        )
                        add(
                            GeneralIndexItem(
                                R.drawable.ic_event_insulin_with_bg,
                                resources.getString(R.string.statistic_general_index_title_insulin),
                                resources.getString(R.string.statistic_general_index_description_by_period_insulin, "3"),
                                "3"
                            )
                        )
                        add(
                            GeneralIndexItem(
                                R.drawable.ic_event_insulin_with_bg,
                                resources.getString(R.string.statistic_general_index_title_bolus_insulin),
                                resources.getString(R.string.statistic_general_index_description_by_period_bolus_insulin, "3"),
                                "3"
                            )
                        )
                        add(
                            GeneralIndexItem(
                                R.drawable.ic_event_insulin_with_bg,
                                resources.getString(R.string.statistic_general_index_title_basal_insulin),
                                resources.getString(R.string.statistic_general_index_description_by_period_basal_insulin, "3"),
                                "3"
                            )
                        )
                        add(
                            GeneralIndexItem(
                                R.drawable.ic_event_activity_with_bg,
                                resources.getString(R.string.statistic_general_index_title_activity),
                                resources.getString(R.string.statistic_general_index_description_by_day_activity, "3", "3"),
                                "3",
                                true
                            )
                        )
                    }
                )
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    fun setPeriod(period: Period) {
        loadScreenAction.consumer.accept(period)
    }

    // TODO: this is simple solution. For GlucoseIndexItem.Type.AVERAGE add checking value in diapason.
    private inline fun GlucoseIndexItem.Type.getBg(): Int =
        when (this) {
            GlucoseIndexItem.Type.AVERAGE -> R.drawable.bg_glucose_index_2
            GlucoseIndexItem.Type.TOTAL -> R.drawable.bg_glucose_index_2
            GlucoseIndexItem.Type.HIGH -> R.drawable.bg_glucose_index_3
            GlucoseIndexItem.Type.NORMAL -> R.drawable.bg_glucose_index_1
            GlucoseIndexItem.Type.LOW -> R.drawable.bg_glucose_index_4
        }

    private fun GlucoseIndexItem.Type.getDescription(): Int =
        when (this) {
            GlucoseIndexItem.Type.AVERAGE -> R.string.statistic_glucose_index_description_average
            GlucoseIndexItem.Type.TOTAL -> R.string.statistic_glucose_index_description_total
            GlucoseIndexItem.Type.HIGH -> R.string.statistic_glucose_index_description_high
            GlucoseIndexItem.Type.NORMAL -> R.string.statistic_glucose_index_description_normal
            GlucoseIndexItem.Type.LOW -> R.string.statistic_glucose_index_description_low
        }
}