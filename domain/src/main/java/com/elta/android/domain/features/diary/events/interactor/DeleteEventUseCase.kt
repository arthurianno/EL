package com.elta.android.domain.features.diary.events.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class DeleteEventUseCase @Inject constructor(
    private val repo: EventsRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<DeleteEventUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        val p = checkNotNull(params)
        return repo.deleteEvent(p.event)
    }

    data class Params(val event: Event)
}
