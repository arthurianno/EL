package com.elta.android.domain.features.diary.events.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetLastInsulinEventUseCase @Inject constructor(
    private val eventsRepo: EventsRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Event, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<Event> {
        return eventsRepo.getLastEvent(EventType.INSULIN)
    }
}
