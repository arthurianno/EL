package com.elta.android.domain.features.diary.home.interactor

import com.elta.android.domain.features.diary.events.model.addTag
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.domain.features.diary.home.model.atEndOfDay
import com.elta.android.domain.features.diary.home.model.atTimeOfDay
import com.elta.android.domain.features.diary.tags.repository.TagsRepository
import com.elta.android.domain.features.user.repository.ProfileRepository
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
    private val profileRepo: ProfileRepository,
    private val schedulers: SchedulersFacade
) : ObservableUseCase<HomeModel, Unit>(schedulers) {
    override fun buildUseCaseObservable(params: Unit?): Observable<HomeModel> {
        val now = Date()
        return Observables.zip(
            eventsRepo.getEvents(now.atTimeOfDay(), now.atEndOfDay()).applyScheduler(schedulers),
            tagsRepo.getTags().applyScheduler(schedulers),
            profileRepo.getProfile().toObservable().applyScheduler(schedulers)
        ).map { triple ->
            val events = triple.first
            val tags = triple.second
            val eventsWithTags = events.map { it.addTag(tags) }
            val glucoseLevelSettings = triple.third.glucoseLevelSettings ?: GlucoseLevelSettings()
            buildHomeModel(eventsWithTags, tags, glucoseLevelSettings)
        }
    }
}