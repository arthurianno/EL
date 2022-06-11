package com.elta.android.domain.features.diary.home.interactor

import com.elta.android.common.utils.atEndOfDay
import com.elta.android.common.utils.atStartOfDay
import com.elta.android.domain.features.diary.events.model.addTag
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.domain.features.diary.tags.repository.TagsRepository
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.elta.android.domain.features.userinfo.repository.UserInfoRepository
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.applyScheduler
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class GetHomeModelUseCase @Inject constructor(
    private val eventsRepo: EventsRepository,
    private val tagsRepo: TagsRepository,
    private val profileRepo: ProfileRepository,
    private val userInfoRepo: UserInfoRepository,
    private val schedulers: SchedulersFacade
) : ObservableUseCase<HomeModel, Unit>(schedulers) {
    override fun buildUseCaseObservable(params: Unit?): Observable<HomeModel> {
        val now = LocalDateTime.now()
        return Observables.zip(
            eventsRepo.getEvents(now.atStartOfDay(), now.atEndOfDay()).applyScheduler(schedulers),
            tagsRepo.getTags().applyScheduler(schedulers),
            profileRepo.getProfile().toObservable().applyScheduler(schedulers),
            userInfoRepo.getUserInfo().toObservable().applyScheduler(schedulers)
        ) { events, tags, profile, userInfo ->
            val eventsWithTags = events.map { it.addTag(tags) }
            val glucoseLevelSettings = profile.glucoseLevelSettings ?: GlucoseLevelSettings()
            buildHomeModel(eventsWithTags, tags, glucoseLevelSettings, userInfo)
        }
    }
}
