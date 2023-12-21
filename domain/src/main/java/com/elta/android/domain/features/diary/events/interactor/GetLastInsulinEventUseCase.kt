package com.elta.android.domain.features.diary.events.interactor

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetLastInsulinEventUseCase @Inject constructor(
    private val eventsRepo: EventsRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<EventV2, GetLastInsulinEventUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<EventV2> {
        checkNotNull(params)
        return eventsRepo.getLastEvent(params.type)
    }

    data class Params(val type: EventType)

}
