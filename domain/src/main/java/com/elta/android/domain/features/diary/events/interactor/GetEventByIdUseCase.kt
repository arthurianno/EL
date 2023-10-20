package com.elta.android.domain.features.diary.events.interactor

import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.addTag
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.tags.repository.TagsRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.applyScheduler
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import javax.inject.Inject

class GetEventByIdUseCase @Inject constructor(
    private val eventsRepo: EventsRepository,
    private val tagsRepo: TagsRepository,
    private val schedulers: SchedulersFacade
) : SingleUseCase<EventV2, GetEventByIdUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<EventV2> =
        Singles.zip(
            eventsRepo.getEventById(checkNotNull(params).id).applyScheduler(schedulers),
            tagsRepo.getTags().singleOrError().applyScheduler(schedulers)
        ).map { (events, tags) ->
            events.addTag(tags)
        }

    data class Params(val id: String)
}
