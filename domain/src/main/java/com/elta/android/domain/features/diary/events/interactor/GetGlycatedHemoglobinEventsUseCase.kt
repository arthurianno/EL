package com.elta.android.domain.features.diary.events.interactor

import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.nullgr.core.interactor.ObservableListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class GetGlycatedHemoglobinEventsUseCase @Inject constructor(
    private val eventsRepo: EventsRepository,
    schedulers: SchedulersFacade
) : ObservableListUseCase<EventV2, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Observable<List<EventV2>> =
        eventsRepo.getEvents().map { buildHemoglobinEvents(it) }
}
