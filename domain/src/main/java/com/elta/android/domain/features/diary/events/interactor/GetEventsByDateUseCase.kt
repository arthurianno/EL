package com.elta.android.domain.features.diary.events.interactor

import com.elta.android.common.utils.atEndOfDay
import com.elta.android.common.utils.atStartOfDay
import com.elta.android.domain.features.diary.events.model.addTag
import com.elta.android.domain.features.diary.events.model.modifyValues
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.home.interactor.getEventsBlocks
import com.elta.android.domain.features.diary.home.interactor.sortAndFilter
import com.elta.android.domain.features.diary.home.model.EventsBlock
import com.elta.android.domain.features.diary.tags.repository.TagsRepository
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.ObservableListUseCase
import com.nullgr.core.rx.applyScheduler
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class GetEventsByDateUseCase @Inject constructor(
    private val eventsRepo: EventsRepository,
    private val profileRepo: ProfileRepository,
    private val tagsRepo: TagsRepository,
    private val schedulers: SchedulersFacade
) : ObservableListUseCase<EventsBlock, GetEventsByDateUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Observable<List<EventsBlock>> {
        val date = checkNotNull(params).date
        return Observables.zip(
            eventsRepo.getEvents(date.atStartOfDay(), date.atEndOfDay()),
            tagsRepo.getTags(),
            profileRepo.getProfile().toObservable()
        )
            .map { (events, tags, profile) ->
                val eventsWithTags = events
                    .modifyValues(profile.glucoseFormat)
                    .map { it.addTag(tags) }
                Pair(eventsWithTags, tags)
            }
            .map { (events, tags) ->
                val sortedEvents = events.sortAndFilter()
                getEventsBlocks(sortedEvents, tags)
            }
            .applyScheduler(schedulers)
    }

    data class Params(val date: LocalDateTime)
}
