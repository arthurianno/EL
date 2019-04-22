package com.elta.android.domain.features.statistics.interactor

import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.domain.features.statistics.model.StatisticPeriod
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import javax.inject.Inject

class GetStatisticByPeriodUseCase @Inject constructor(
    private val eventsRepo: EventsRepository,
    private val userRepo: ProfileRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<StatisticByPeriodModel, GetStatisticByPeriodUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<StatisticByPeriodModel> {
        val p = checkNotNull(params)
        return Singles.zip(
            eventsRepo.getEvents(p.period.start, p.period.end).single(emptyList()),
            userRepo.getProfile()
        )
            .map { pair ->
                buildStatisticModel(
                    period = p.period,
                    events = pair.first,
                    settings = pair.second.glucoseLevelSettings ?: GlucoseLevelSettings()
                )
            }
    }

    data class Params(
        val period: StatisticPeriod
    )
}