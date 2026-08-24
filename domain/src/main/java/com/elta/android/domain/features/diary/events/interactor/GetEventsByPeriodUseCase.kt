package com.elta.android.domain.features.diary.events.interactor

import com.elta.android.common.utils.atEndOfDay
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.nullgr.core.interactor.ObservableListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import org.threeten.bp.LocalDate
import javax.inject.Inject

class GetEventsByPeriodUseCase @Inject constructor(
    private val eventsRepository: EventsRepository,
    schedulers: SchedulersFacade
) : ObservableListUseCase<EventV2, GetEventsByPeriodUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Observable<List<EventV2>> {
        val period = checkNotNull(params)
        return eventsRepository.getEvents(
            period.start.atStartOfDay(),
            period.end.atStartOfDay().atEndOfDay()
        )
    }

    data class Params(
        val start: LocalDate,
        val end: LocalDate
    )
}
