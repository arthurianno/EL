@file:Suppress("MaxLineLength", "LongMethod")

package com.elta.android.presentation.features.statistic.period.pm

import com.elta.android.domain.features.statistics.interactor.GetStatisticByPeriodUseCase
import com.elta.android.domain.features.statistics.model.Periods
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.statistic.period.ui.Period
import io.reactivex.Observable
import javax.inject.Inject

class PeriodPm @Inject constructor(
    private val getStatisticByPeriodUseCase: GetStatisticByPeriodUseCase,
    private val periodBuilder: StatisticByPeriodItemsBuilder,
    services: ServiceFacade
) : BaseListPm(services) {

    val loadScreenAction = Action<Period>()

    private val periodState = State<Period>()

    override fun onCreate() {
        super.onCreate()

        loadScreenAction.observable
            .skipWhileInProgress()
            .map(::createUseCaseParams)
            .flatMapSingle { params ->
                getStatisticByPeriodUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnSuccess(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        Observable.merge(
            bus.events<Events.EventsChanged>().map { Unit },
            bus.events<Events.ProfileUpdated>().map { Unit }
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

    private fun handleSuccess(model: StatisticByPeriodModel) {
        items.consumer.accept(periodBuilder.build(model))
    }
}