package com.elta.android.domain.features.diary.events.interactor

import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class UpdateEventUseCase @Inject constructor(
    private val repo: EventsRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<UpdateEventUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable =
        repo.updateEvent(checkNotNull(params).event)

    data class Params(val event: EventV2)
}
