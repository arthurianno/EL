package com.elta.android.presentation.features.statistic.period.pm

import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.elta.android.presentation.features.statistic.period.ui.Period
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexItem
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
                        GlucoseIndexItem.Type.values().forEach {
                            add(GlucoseIndexItem(it, resources.getDrawable(it.getBg()), "value", "unit", resources.getString(it.getDescription())))
                        }
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