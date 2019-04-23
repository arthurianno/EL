package com.elta.android.domain.features.diary.events.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.nullgr.core.interactor.ObservableListUseCase
import com.nullgr.core.rx.applyScheduler
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class GetGlycatedHemoglobinEventsUseCase @Inject constructor(
    private val eventsRepo: EventsRepository,
    private val schedulers: SchedulersFacade
) : ObservableListUseCase<Event, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Observable<List<Event>> {
        return eventsRepo.getEvents().applyScheduler(schedulers)
            .map { buildHemoglobinEvents(it) }
    }
}