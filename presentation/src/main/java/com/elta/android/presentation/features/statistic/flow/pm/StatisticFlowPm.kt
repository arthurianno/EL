package com.elta.android.presentation.features.statistic.flow.pm

import android.net.Uri
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.getPeriodParam
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventParam
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.statistic.period.ui.Period
import com.elta.android.presentation.messages.SnackBarMessageData
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StatisticFlowPm @Inject constructor(
    services: ServiceFacade
) : BaseFlowPm(services) {

    val periodSelectedAction = action<Int>()
    val selectedPeriodIdState = state(R.id.periodSevenDaysView)
    val menuAction = action<Unit>()
    val showReportPeriodChooser = command<Unit>(bufferSize = 1)

    override fun onCreate() {
        super.onCreate()

        periodSelectedAction.observable
            .map { it to handlePeriodTabClick(it) }
            .trackEvent {
                AnalyticsEvent(
                    AnalyticsEventType.PERIOD_TAB,
                    hashMapOf(AnalyticsEventParam.PERIOD to getPeriodParam(it.second.count))
                )
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

    fun selectPeriod(period: Period) {
        val id = when (period) {
            Period.SEVEN -> R.id.periodSevenDaysView
            Period.FOURTEEN -> R.id.periodFourteenDaysView
            Period.THIRTY -> R.id.periodThirtyDaysView
            Period.NINETY -> R.id.periodNinetyDaysView
        }
        periodSelectedAction.consumer.accept(id)
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
