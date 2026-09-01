@file:Suppress("MaxLineLength", "LongMethod")

package com.elta.android.presentation.features.statistic.period.pm

import com.elta.android.domain.features.statistics.interactor.GetStatisticByPeriodUseCase
import com.elta.android.domain.features.statistics.model.Periods
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.date.DateChangedEvent
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.statistic.period.ui.Period
import io.reactivex.Observable
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import javax.inject.Inject

class PeriodPm @Inject constructor(
    private val getStatisticByPeriodUseCase: GetStatisticByPeriodUseCase,
    services: ServiceFacade
) : BaseListPm(services) {

    private val loadScreenAction = action<Period>()
    private val periodState = state<Period>()
    val statisticsByPeriodState = state<StatisticByPeriodModel>()

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
}
