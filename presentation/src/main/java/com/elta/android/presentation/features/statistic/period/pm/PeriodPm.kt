@file:Suppress("MaxLineLength", "LongMethod")

package com.elta.android.presentation.features.statistic.period.pm

import com.elta.android.domain.features.statistics.interactor.GetStatisticByPeriodUseCase
import com.elta.android.domain.features.statistics.model.Periods
import com.elta.android.domain.features.statistics.model.StatisticPeriod
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.date.DateChangedEvent
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.statistic.period.ui.Period
import com.elta.android.presentation.features.statistic.period.model.StatisticsBlock
import com.elta.android.presentation.features.statistic.period.preferences.StatisticsBlockPreferences
import io.reactivex.Observable
import io.reactivex.Single
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import javax.inject.Inject

class PeriodPm @Inject constructor(
    private val getStatisticByPeriodUseCase: GetStatisticByPeriodUseCase,
    private val blockPreferences: StatisticsBlockPreferences,
    services: ServiceFacade
) : BasePm(services) {

    private val loadScreenAction = action<Period>()
    private val periodState = state<Period>()
    val statisticsByPeriodState = state<StatisticsPeriodModels>()
    val visibleBlocksState = state<List<StatisticsBlock>>()

    override fun onCreate() {
        super.onCreate()
        visibleBlocksState.consumer.accept(blockPreferences.read())

        loadScreenAction.observable
            .skipWhileInProgress()
            .map(::createPeriods)
            .flatMapSingle { periods ->
                Single.zip(
                    getStatisticByPeriodUseCase.execute(GetStatisticByPeriodUseCase.Params(periods.current)),
                    getStatisticByPeriodUseCase.execute(GetStatisticByPeriodUseCase.Params(periods.previous))
                ) { current, previous ->
                    StatisticsPeriodModels(current = current, previous = previous)
                }
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

    fun saveVisibleBlocks(blocks: List<StatisticsBlock>) {
        blockPreferences.save(blocks)
        visibleBlocksState.consumer.accept(blocks)
    }

    private fun createPeriods(period: Period): StatisticsPeriods {
        val current = when (period) {
            Period.SEVEN -> Periods.SevenDays()
            Period.FOURTEEN -> Periods.FourteenDays()
            Period.THIRTY -> Periods.ThirtyDays()
            Period.NINETY -> Periods.NinetyDays()
        }
        val periodLength = when (period) {
            Period.SEVEN -> 7L
            Period.FOURTEEN -> 14L
            Period.THIRTY -> 30L
            Period.NINETY -> 90L
        }
        return StatisticsPeriods(
            current = current,
            previous = FixedStatisticPeriod(
                start = current.start.minusDays(periodLength),
                end = current.start.minusNanos(1)
            )
        )
    }
}

data class StatisticsPeriodModels(
    val current: StatisticByPeriodModel,
    val previous: StatisticByPeriodModel
)

private data class StatisticsPeriods(
    val current: StatisticPeriod,
    val previous: StatisticPeriod
)

private data class FixedStatisticPeriod(
    override val start: org.threeten.bp.LocalDateTime,
    override val end: org.threeten.bp.LocalDateTime
) : StatisticPeriod
