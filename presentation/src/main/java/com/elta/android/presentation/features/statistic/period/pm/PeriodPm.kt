@file:Suppress("MaxLineLength", "LongMethod")

package com.elta.android.presentation.features.statistic.period.pm

import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.statistics.interactor.GetStatisticByPeriodUseCase
import com.elta.android.domain.features.statistics.model.Periods
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.date.DateChangedEvent
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.statistic.period.ui.Period
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseStatisticChartItem
import io.reactivex.Observable
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import org.threeten.bp.LocalDate
import javax.inject.Inject

private const val STATISTIC_CHART_DATE_FORMAT = "dd MMM"

class PeriodPm @Inject constructor(
    private val getStatisticByPeriodUseCase: GetStatisticByPeriodUseCase,
    private val periodBuilder: StatisticByPeriodItemsBuilder,
    services: ServiceFacade
) : BaseListPm(services) {

    private val loadScreenAction = action<Period>()
    private val periodState = state<Period>()
    private val statisticsByPeriodState = state<StatisticByPeriodModel>()
    val chartModel = state<GlucoseStatisticChartItem>()

    override fun onCreate() {
        super.onCreate()

        loadScreenAction.observable
            .skipWhileInProgress()
            .map(::createUseCaseParams)
            .flatMapSingle { params ->
                getStatisticByPeriodUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnSuccess {
                        statisticsByPeriodState.consumer.accept(it)
                        chartModel.consumer.accept(it.toChartItem(null))
                    }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        Observable.merge(
            bus.events<Events.EventsChanged>().map { Unit },
            bus.events<Events.ProfileUpdated>().map { Unit },
            bus.events<DateChangedEvent>().map { Unit }
        )
            .filter { periodState.hasValue() }
            .map { periodState.value }
            .subscribe(loadScreenAction.consumer)
            .untilDestroy()

        statisticsByPeriodState.observable
            .map { buildItems(it) }
            .subscribe(items.consumer)
            .untilDestroy()
    }

    override fun onBind() {
        super.onBind()
        bus.clicks<Clicks.DateInStatisticsClicked>()
            .map {
                chartModel.consumer.accept(statisticsByPeriodState.value.toChartItem(it.date))
                buildItems(statisticsByPeriodState.value, it.date)
            }
            .subscribe(items.consumer)
            .untilUnbind()
    }

    fun setPeriod(period: Period) {
        periodState.consumer.accept(period)
        loadScreenAction.consumer.accept(period)
    }

    private fun createUseCaseParams(period: Period): GetStatisticByPeriodUseCase.Params =
        GetStatisticByPeriodUseCase.Params(
            when (period) {
                Period.SEVEN -> Periods.SevenDays()
                Period.FOURTEEN -> Periods.FourteenDays()
                Period.THIRTY -> Periods.ThirtyDays()
                Period.NINETY -> Periods.NinetyDays()
            }
        )

    private fun StatisticByPeriodModel.toChartItem(selectedDate: LocalDate?) =
        GlucoseStatisticChartItem(
            datesTitle = resources.getString(
                R.string.statistic_chart_period_dates_mask,
                period.start.toStringWithFormat(STATISTIC_CHART_DATE_FORMAT),
                period.end.toStringWithFormat(STATISTIC_CHART_DATE_FORMAT)
            ),
            chartModel = this.toChartModel(selectedDate)
        )

    private fun buildItems(model: StatisticByPeriodModel, date: LocalDate? = null) =
        periodBuilder.build(model, date)
}
