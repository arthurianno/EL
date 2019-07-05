package com.elta.android.presentation.features.statistic.flow.pm

import android.net.Uri
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytics.getPeriodParam
import com.elta.android.presentation.analytics.model.AnalyticsEvent
import com.elta.android.presentation.analytics.model.AnalyticsEventParam
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.statistic.period.ui.Period
import com.elta.android.presentation.messages.SnackBarMessageData
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StatisticFlowPm @Inject constructor(
    services: ServiceFacade
) : BaseFlowPm(services) {

    val periodSelectedAction = Action<Int>()
    val selectedPeriodIdState = State(R.id.periodSevenDaysView)
    val menuAction = Action<Unit>()
    val showReportPeriodChooser = Command<Unit>(bufferSize = 1)

    override fun onCreate() {
        super.onCreate()

        periodSelectedAction.observable
            .map { it to handlePeriodTabClick(it) }
            .trackEvent {
                AnalyticsEvent(
                    AnalyticsEventType.PERIOD_TAB,
                    hashMapOf(AnalyticsEventParam.PERIOD to getPeriodParam(it.second.count)))
            }
            .doOnNext { router.navigateToTab(Screens.PeriodScreen(it.second)) }
            .map { it.first }
            .subscribe(selectedPeriodIdState.consumer)
            .untilDestroy()

        menuAction.observable
            .subscribe(showReportPeriodChooser.consumer)
            .untilDestroy()

        bus.events<Events.ReportLoadedEvent>()
            .delay(CHOOSER_DELAY, TimeUnit.MILLISECONDS)
            .map { it.uri }
            .doOnNext(::handleFileUri)
            .subscribe()
            .untilDestroy()
    }

    override fun navigateToLaunchScreen() {
        router.navigateToTab(Screens.PeriodScreen(Period.SEVEN))
    }

    private fun handleFileUri(uri: Uri) {
        if (uri != Uri.EMPTY)
            router.navigateTo(Screens.ViewPdfScreen(uri))
        else
            showSnackBar(SnackBarMessageData.SimpleTextMessage(resources.getString(R.string.error_file_not_saved)))
    }

    private fun handlePeriodTabClick(id: Int): Period =
        when (id) {
            R.id.periodSevenDaysView -> Period.SEVEN
            R.id.periodFourteenDaysView -> Period.FOURTEEN
            R.id.periodThirtyDaysView -> Period.THIRTY
            R.id.periodNinetyDaysView -> Period.NINETY
            else -> throw IllegalArgumentException("$id is not supported.")
        }

    companion object {
        private const val CHOOSER_DELAY = 400L
    }
}