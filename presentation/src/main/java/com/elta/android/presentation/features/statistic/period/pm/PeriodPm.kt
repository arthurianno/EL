package com.elta.android.presentation.features.statistic.period.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.elta.android.presentation.features.statistic.period.ui.Period
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
                    listOf(
                        ProfileSettingsHeaderItem("$it")
                    )
                )
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    fun setPeriod(period: Period) {
        loadScreenAction.consumer.accept(period)
    }
}