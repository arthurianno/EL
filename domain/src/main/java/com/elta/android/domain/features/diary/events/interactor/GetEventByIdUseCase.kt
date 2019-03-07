package com.elta.android.domain.features.diary.events.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetEventByIdUseCase @Inject constructor(
    private val repo: EventsRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Event, GetEventByIdUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<Event> =
        repo.getEventById(checkNotNull(params).id)

    data class Params(val id: String)
}