package com.elta.android.domain.features.diary.home.interactor

import com.elta.android.domain.features.diary.events.model.addTag
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.domain.features.diary.home.model.atEndOfDay
import com.elta.android.domain.features.diary.home.model.atTimeOfDay
import com.elta.android.domain.features.diary.tags.repository.TagsRepository
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.applyScheduler
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import java.util.Date
import javax.inject.Inject

class GetHomeModelUseCase @Inject constructor(
    private val eventsRepo: EventsRepository,
    private val tagsRepo: TagsRepository,
    private val schedulers: SchedulersFacade
) : ObservableUseCase<HomeModel, Unit>(schedulers) {
    override fun buildUseCaseObservable(params: Unit?): Observable<HomeModel> {
        val now = Date()
        return Observables.zip(
            eventsRepo.getEvents(now.atTimeOfDay(), now.atEndOfDay()).applyScheduler(schedulers),
            tagsRepo.getTags().applyScheduler(schedulers)
        ).map { pair ->
            val events = pair.first
            val tags = pair.second
            val eventsWithTags = events.map { it.addTag(tags) }
            Pair(eventsWithTags, tags)
        }.map {
            // TODO: add real glucose level settings
            buildHomeModel(it.first, it.second, GlucoseLevelSettings())
        }
    }
}