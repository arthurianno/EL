package com.elta.android.presentation.features.statistic.flow.pm

import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.statistic.period.ui.Period
import javax.inject.Inject

class StatisticFlowPm @Inject constructor(
    services: ServiceFacade
) : BaseFlowPm(services) {

    val periodSelectedAction = Action<Int>()
    val selectedPeriodIdState = State(R.id.periodSevenDaysView)

    override fun onCreate() {
        super.onCreate()

        periodSelectedAction.observable
            .doOnNext(::handlePeriodTabClick)
            .subscribe(selectedPeriodIdState.consumer)
            .untilDestroy()
    }

    override fun navigateToLaunchScreen() {
        router.navigateToTab(Screens.PeriodScreen(Period.SEVEN))
    }

    private fun handlePeriodTabClick(id: Int) {
        when (id) {
            R.id.periodSevenDaysView -> router.navigateToTab(Screens.PeriodScreen(Period.SEVEN))
            R.id.periodFourteenDaysView -> router.navigateToTab(Screens.PeriodScreen(Period.FOURTEEN))
            R.id.periodThirtyDaysView -> router.navigateToTab(Screens.PeriodScreen(Period.THIRTY))
            R.id.periodNinetyDaysView -> router.navigateToTab(Screens.PeriodScreen(Period.NINETY))
        }
    }
}