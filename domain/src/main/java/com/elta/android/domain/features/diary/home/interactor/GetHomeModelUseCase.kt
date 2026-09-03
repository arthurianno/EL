package com.elta.android.domain.features.diary.home.interactor

import com.elta.android.domain.features.diary.events.model.addTag
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.home.model.CalculatorFlow.Companion.toCalculatorFlow
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
        val rangeStart = now.minusHours(HOME_SUMMARY_RANGE_HOURS)
        return Observables.zip(
            eventsRepo.getEvents(rangeStart, now).applyScheduler(schedulers),
            tagsRepo.getTags().applyScheduler(schedulers),
            profileRepo.getProfile().toObservable().applyScheduler(schedulers),
            userInfoRepo.getUserInfo().toObservable().applyScheduler(schedulers)
        ) { events, tags, profile, userInfo ->
            buildHomeModel(
                events = events.map { it.addTag(tags) },
                tags = tags,
                settings = profile.glucoseLevelSettings,
                userInfo = userInfo,
                glucoseFormat = profile.glucoseFormat,
                calculatorFlow = profile.diabetes.toCalculatorFlow(events)
            )
        }
    }

    private companion object {
        const val HOME_SUMMARY_RANGE_HOURS = 24L
    }
}
